package com.example.trello.service;

import com.example.trello.dto.request.CommentCreateRequest;
import com.example.trello.dto.request.CommentUpdateRequest;
import com.example.trello.dto.response.CommentResponse;
import com.example.trello.exception.AppException;
import com.example.trello.exception.ErrorCode;
import com.example.trello.mapper.CommentMapper;
import com.example.trello.model.Comment;
import com.example.trello.repository.CommentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService {
    CommentRepository commentRepository;
    CommentMapper commentMapper;

    // --- 1. CREATE ---
    public CommentResponse createComment(CommentCreateRequest request) {
        Comment comment = commentMapper.toComment(request);

        comment = commentRepository.save(comment);
        // TODO: Gửi Notifications cho users được mention (@...)
        // TODO: Ghi Activity log

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
    public CommentResponse updateComment(String commentId, CommentUpdateRequest request) {
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
        }

        // Trả về response (có thể là comment cũ nếu không có gì thay đổi)
        return commentMapper.toCommentResponse(existingComment);
    }

    // --- 4. DELETE (Hard Delete) ---
    public void deleteComment(String commentId) {
        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        commentRepository.delete(existingComment);
        // TODO: Ghi Activity log (comment_deleted)
    }
}