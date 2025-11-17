package com.example.trello.service;

import com.example.trello.dto.request.ActivityCreateRequest;
import com.example.trello.dto.request.CommentCreateRequest;
import com.example.trello.dto.request.CommentUpdateRequest;
import com.example.trello.dto.request.NotificationCreateRequest;
import com.example.trello.dto.response.CommentResponse;
import com.example.trello.dto.response.WebSocketUpdateResponse;
import com.example.trello.enums.WebSocketUpdateType;
import com.example.trello.exception.AppException;
import com.example.trello.exception.ErrorCode;
import com.example.trello.mapper.CommentMapper;
import com.example.trello.model.Card;
import com.example.trello.model.Comment;
import com.example.trello.model.Notification;
import com.example.trello.model.User;
import com.example.trello.repository.CardRepository;
import com.example.trello.repository.CommentRepository;
import com.example.trello.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService {
    CommentRepository commentRepository;
    CommentMapper commentMapper;

    ActivityService activityService;
    NotificationService notificationService;
    UserRepository userRepository;
    CardRepository cardRepository;

    SimpMessagingTemplate messagingTemplate;

    // --- 1. CREATE ---
    public CommentResponse createComment(CommentCreateRequest request) {
        Comment comment = commentMapper.toComment(request);

        comment = commentRepository.save(comment);
        // TODO: Gửi Notifications cho users được mention (@...)

        User commenter = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)); // Giả định user luôn tồn tại
        Card card = cardRepository.findById(request.getCardId())
                .orElseThrow(() -> new AppException(ErrorCode.CARD_NOT_FOUND));

        // TODO: Ghi Activity log

        if (commenter != null) {
            String logMessage = String.format("%s đã bình luận trên thẻ này: \"%s\"",
                    commenter.getDisplayName(),
                    request.getContent().length() > 30 ? request.getContent().substring(0, 30) + "..." : request.getContent()
            );

            activityService.createActivity(ActivityCreateRequest.builder()
                    .boardId(request.getBoardId())
                    .cardId(request.getCardId())
                    .userId(request.getUserId())
                    .action("add_comment")
                    .details(Map.of("text", logMessage, "commentId", comment.getId()))
                    .build());
        }
        if (commenter != null && card != null && request.getMentions() != null) {
            String title = String.format("%s đã nhắc đến bạn", commenter.getDisplayName());
            String message = String.format("Trong thẻ '%s': %s", card.getTitle(), request.getContent());

            Notification.NotificationLink link = Notification.NotificationLink.builder()
                    .type("card")
                    .boardId(request.getBoardId())
                    .cardId(request.getCardId())
                    .build();

            for (String mentionedUserId : request.getMentions()) {
                if (!mentionedUserId.equals(request.getUserId())) { // Không tự thông báo cho mình
                    notificationService.createNotification(NotificationCreateRequest.builder()
                            .userId(mentionedUserId) // Người nhận
                            .type("mention_comment")
                            .title(title)
                            .message(message)
                            .link(link)
                            .triggeredBy(request.getUserId()) // Người gửi
                            .build());
                }
            }
        }
        broadcastCommentUpdate(comment, WebSocketUpdateType.COMMENT_CREATED);
        return commentMapper.toCommentResponse(comment);
    }

    // --- 2. READ (Single) ---
    public CommentResponse getCommentById(String commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
        return commentMapper.toCommentResponse(comment);
    }

    // --- 2. READ (All by Card) ---
    public List<CommentResponse> getCommentsByCard(String cardId) {
        List<Comment> comments = commentRepository.findByCardIdOrderByCreatedAtDesc(cardId);
        return comments.stream()
                .map(commentMapper::toCommentResponse)
                .collect(Collectors.toList());
    }

    // --- 3. UPDATE ---
    public CommentResponse updateComment(String commentId, CommentUpdateRequest request,String updatedByUserId) {
        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        // Kiểm tra nội dung có thay đổi không
        if (request.getContent() != null && !request.getContent().equals(existingComment.getContent())) {

            // Cập nhật trường isEdited và editedAt
            existingComment.setEdited(true);
            existingComment.setEditedAt(Instant.now());

            // Map nội dung và cập nhật updatedAt
            commentMapper.updateComment(existingComment, request);

            existingComment = commentRepository.save(existingComment);
            // TODO: Ghi Activity log (comment_updated)
            User commenter = userRepository.findById(existingComment.getUserId()).orElse(null);
            if (commenter != null) {
                String logMessage = String.format("%s đã chỉnh sửa bình luận", commenter.getDisplayName());
                activityService.createActivity(ActivityCreateRequest.builder()
                        .boardId(existingComment.getBoardId())
                        .cardId(existingComment.getCardId())
                        .userId(updatedByUserId)
                        .action("update_comment")
                        .details(Map.of("text", logMessage, "commentId", commentId))
                        .build());
            }
        }
        broadcastCommentUpdate(existingComment, WebSocketUpdateType.COMMENT_UPDATED);
        // Trả về response (có thể là comment cũ nếu không có gì thay đổi)
        return commentMapper.toCommentResponse(existingComment);
    }

    // --- 4. DELETE (Hard Delete) ---
    public void deleteComment(String commentId,String deletedByUserId) {
        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        commentRepository.delete(existingComment);
        User commenter = userRepository.findById(existingComment.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // TODO: Ghi Activity log (comment_deleted)
        if (commenter != null) {
            String logMessage = String.format("%s đã xóa một bình luận", commenter.getDisplayName());
            activityService.createActivity(ActivityCreateRequest.builder()
                    .boardId(existingComment.getBoardId())
                    .cardId(existingComment.getCardId())
                    .userId(deletedByUserId)
                    .action("delete_comment")
                    .details(Map.of("text", logMessage))
                    .build());
        }
        broadcastCommentUpdate(existingComment, WebSocketUpdateType.COMMENT_DELETED);
    }
    private void broadcastCommentUpdate(Comment comment, WebSocketUpdateType type) {
        CommentResponse response = commentMapper.toCommentResponse(comment);
        WebSocketUpdateResponse wsResponse = WebSocketUpdateResponse.builder()
                .type(type)
                .payload(response)
                .build();

        // Gửi tin nhắn đến kênh của board
        messagingTemplate.convertAndSend("/topic/board/" + comment.getBoardId(), wsResponse);
    }
}