package com.example.trello.service;

import com.example.trello.dto.request.*;
import com.example.trello.dto.response.BoardResponse;
import com.example.trello.dto.response.WebSocketUpdateResponse;
import com.example.trello.enums.WebSocketUpdateType;
import com.example.trello.exception.AppException;
import com.example.trello.exception.ErrorCode;
import com.example.trello.mapper.BoardMapper;
import com.example.trello.model.Board;
import com.example.trello.model.Notification;
import com.example.trello.model.User;
import com.example.trello.repository.BoardRepository;
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
public class BoardService {
    BoardRepository boardRepository;
    BoardMapper boardMapper;
    UserRepository userRepository;
    EmailService emailService;

    ActivityService activityService;
    NotificationService notificationService;
    SimpMessagingTemplate messagingTemplate;

    // --- 1. CREATE ---
    public BoardResponse createBoard(BoardCreateRequest request) {
        Board board = boardMapper.toBoard(request);

        // Thêm thành viên Owner mặc định
        Board.BoardMember ownerMember = Board.BoardMember.builder()
                .userId(request.getOwnerId())
                .role(Board.BoardMember.MemberRole.OWNER)
                .addedAt(Instant.now()) // Dùng Instant
                .addedBy(request.getOwnerId())
                .build();
        board.setMembers(List.of(ownerMember));

        board = boardRepository.save(board);
        User creator = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (creator != null) {
            activityService.createActivity(ActivityCreateRequest.builder()
                    .boardId(board.getId())
                    .userId(request.getOwnerId())
                    .action("create_board")
                    .details(Map.of("text", String.format("%s đã tạo bảng này", creator.getDisplayName())))
                    .build());
        }
        return boardMapper.toBoardResponse(board);
    }

    // --- 2. READ (Single) ---
    public BoardResponse getBoardById(String boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));
        return boardMapper.toBoardResponse(board);
    }

    // --- 2. READ
    public List<BoardResponse> getBoardsByUserId(String userId) {
        List<Board> boards = boardRepository.findByOwnerIdOrMembers_UserIdAndIsArchivedFalse(userId, userId);
        return boards.stream()
                .map(boardMapper::toBoardResponse)
                .collect(Collectors.toList());
    }

    // --- 3. UPDATE ---
    public BoardResponse updateBoard(String boardId, BoardUpdateRequest request,String updatedByUserId) {
        Board existingBoard = boardRepository.findById(boardId)
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));
        String oldTitle = existingBoard.getTitle();
        boardMapper.updateBoard(existingBoard, request);

        existingBoard = boardRepository.save(existingBoard);
        if (request.getTitle() != null && !request.getTitle().equals(oldTitle)) {
            User updater = userRepository.findById(updatedByUserId).orElse(null);
            if (updater != null) {
                String logMessage = String.format("%s đã đổi tên bảng từ '%s' thành '%s'",
                        updater.getDisplayName(), oldTitle, existingBoard.getTitle());
                activityService.createActivity(ActivityCreateRequest.builder()
                        .boardId(boardId)
                        .userId(updatedByUserId)
                        .action("update_board")
                        .details(Map.of("text", logMessage))
                        .build());
            }
        }
        broadcastBoardUpdate(existingBoard, WebSocketUpdateType.BOARD_UPDATED);
        return boardMapper.toBoardResponse(existingBoard);
    }

    // --- 4. DELETE (Close/Archive) ---
    public void closeBoard(String boardId,String userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));

        board.setClosed(true);
        boardRepository.save(board);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (user != null) {
            String logMessage = String.format("%s đã đóng bảng này", user.getDisplayName());
            activityService.createActivity(ActivityCreateRequest.builder()
                    .boardId(boardId)
                    .userId(userId)
                    .action("close_board")
                    .details(Map.of("text", logMessage))
                    .build());
        }
        // --- GỬI WEBSOCKET ---
        broadcastBoardUpdate(board, WebSocketUpdateType.BOARD_UPDATED);
    }
    // ---Thêm thành viên (FR7) ---
    public BoardResponse addMember(String boardId, BoardMemberRequest request) {
        Board existingBoard = boardRepository.findById(boardId)
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));

        // 1. Kiểm tra User tồn tại (Giả định USER_NOT_FOUND)
         if (!userRepository.existsById(request.getUserId())) {
             throw new AppException(ErrorCode.USER_NOT_FOUND);
         }

        // 2. Kiểm tra User đã là thành viên chưa
        boolean alreadyMember = existingBoard.getMembers().stream()
                .anyMatch(member -> member.getUserId().equals(request.getUserId()));

        if (alreadyMember) {
            throw new AppException(ErrorCode.MEMBER_ALREADY_EXISTS); // Giả định ErrorCode
        }

        // 3. Tạo đối tượng BoardMember mới
        Board.BoardMember newMember = Board.BoardMember.builder()
                .userId(request.getUserId())
                // Chuyển đổi String Role sang Enum
                .role(Board.BoardMember.MemberRole.valueOf(request.getRole().toUpperCase()))
                .addedAt(Instant.now())
                .addedBy(request.getAddedBy())
                .build();

        // 4. Thêm vào danh sách và lưu
        existingBoard.getMembers().add(newMember);

        Board updatedBoard = boardRepository.save(existingBoard);

        User inviter = userRepository.findById(request.getAddedBy())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        String inviterDisplayName = userRepository.findById(request.getAddedBy())
                .map(User::getDisplayName)
                .orElse("Hệ thống"); // Fallback nếu không tìm thấy người mời
        String subject = "[Trello Clone] Lời mời tham gia dự án: " + existingBoard.getTitle();
        String body = String.format(
                "Xin chào!\n\n" +
                        "Bạn đã được mời tham gia dự án '%s' trên Trello Clone.\n\n" +
                        "Vai trò của bạn: %s\n" +
                        "Người mời: %s\n\n" + // <-- Dùng displayName đã tìm được
                        "Hãy đăng nhập vào hệ thống để bắt đầu cộng tác ngay!",
                existingBoard.getTitle(),
                request.getRole().toUpperCase(),
                inviterDisplayName
        );

        emailService.sendNotificationEmail(
                request.getUserId(),
                subject,
                body
        );

        // TODO: Ghi Activity Log: member_added
        // TODO: Gửi Notification cho thành viên mới
        User newMemberUser = userRepository.findById(request.getUserId()).orElse(null);

        if (inviter != null && newMemberUser != null) {
            // --- GHI ACTIVITY LOG (MEMBER_ADDED) ---
            String logMessage = String.format("%s đã thêm %s vào bảng này với vai trò %s",
                    inviter.getDisplayName(),
                    newMemberUser.getDisplayName(),
                    request.getRole().toUpperCase()
            );
            activityService.createActivity(ActivityCreateRequest.builder()
                    .boardId(boardId)
                    .userId(request.getAddedBy())
                    .action("add_member")
                    .details(Map.of("text", logMessage, "addedUserId", newMemberUser.getId()))
                    .build());

            // --- GỬI NOTIFICATION CHO THÀNH VIÊN MỚI ---
            String title = String.format("%s đã mời bạn vào một bảng", inviter.getDisplayName());
            String message = String.format("Bảng: '%s'", existingBoard.getTitle());
            Notification.NotificationLink link = Notification.NotificationLink.builder()
                    .type("board")
                    .boardId(boardId)
                    .build();

            notificationService.createNotification(NotificationCreateRequest.builder()
                    .userId(request.getUserId()) // Người nhận
                    .type("board_invite")
                    .title(title)
                    .message(message)
                    .link(link)
                    .triggeredBy(request.getAddedBy()) // Người mời
                    .build());
        }
        broadcastBoardMemberUpdate(boardId, WebSocketUpdateType.BOARD_MEMBER_ADDED, newMember);
        return boardMapper.toBoardResponse(updatedBoard);
    }

    // --- BỔ SUNG: Xóa thành viên (FR7) ---
    public void removeMember(String boardId, String userIdToRemove,String removedByUserId) {
        Board existingBoard = boardRepository.findById(boardId)
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));

        // 1. Loại bỏ thành viên khỏi danh sách
        boolean removed = existingBoard.getMembers().removeIf(
                member -> member.getUserId().equals(userIdToRemove) && member.getRole() != Board.BoardMember.MemberRole.OWNER
        );

        if (!removed) {
            // Có thể là thành viên không tồn tại hoặc đó là Owner
            throw new AppException(ErrorCode.MEMBER_NOT_FOUND_OR_IS_OWNER); // Giả định ErrorCode
        }

        // 2. Lưu thay đổi
        boardRepository.save(existingBoard);

        // TODO: Ghi Activity Log: member_removed
        User removedUser = userRepository.findById(userIdToRemove)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (removedUser != null) {
            String logMessage = String.format("%s đã bị xóa khỏi bảng này", removedUser.getDisplayName());
            activityService.createActivity(ActivityCreateRequest.builder()
                    .boardId(boardId)
                    .userId("SYSTEM") // Placeholder - Nên lấy từ Security Principal
                    .action("remove_member")
                    .details(Map.of("text", logMessage, "removedUserId", userIdToRemove))
                    .build());
        }
        broadcastBoardMemberUpdate(boardId, WebSocketUpdateType.BOARD_MEMBER_REMOVED, Map.of("userId", userIdToRemove));
    }
    private void broadcastBoardUpdate(Board board, WebSocketUpdateType type) {
        BoardResponse boardResponse = boardMapper.toBoardResponse(board);
        WebSocketUpdateResponse wsResponse = WebSocketUpdateResponse.builder()
                .type(type)
                .payload(boardResponse)
                .build();
        messagingTemplate.convertAndSend("/topic/board/" + board.getId(), wsResponse);
    }
    private void broadcastBoardMemberUpdate(String boardId, WebSocketUpdateType type, Object payload) {
        WebSocketUpdateResponse wsResponse = WebSocketUpdateResponse.builder()
                .type(type)
                .payload(payload)
                .build();
        messagingTemplate.convertAndSend("/topic/board/" + boardId, wsResponse);
    }
}
