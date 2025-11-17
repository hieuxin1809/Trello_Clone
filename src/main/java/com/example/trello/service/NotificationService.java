package com.example.trello.service;

import com.example.trello.dto.request.NotificationCreateRequest;
import com.example.trello.dto.request.NotificationUpdateRequest;
import com.example.trello.dto.response.NotificationResponse;
import com.example.trello.dto.response.WebSocketUpdateResponse;
import com.example.trello.enums.WebSocketUpdateType;
import com.example.trello.exception.AppException;
import com.example.trello.exception.ErrorCode;
import com.example.trello.mapper.NotificationMapper;
import com.example.trello.model.Notification;
import com.example.trello.repository.NotificationRepository;
import com.example.trello.repository.NotificationTokenRepository;
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
public class NotificationService {
    NotificationRepository notificationRepository;
    NotificationMapper notificationMapper;
    FirebaseNotificationService firebaseNotificationService;
    NotificationTokenRepository tokenRepository;

    SimpMessagingTemplate messagingTemplate;
    UserRepository userRepository;

    // --- 1. CREATE (Được gọi nội bộ khi có sự kiện: comment, assign, due date,...) ---
    public NotificationResponse createNotification(NotificationCreateRequest request) {
        Notification notification = notificationMapper.toNotification(request);
        Notification saved = notificationRepository.save(notification);
        Map<String, String> dataPayload = Map.of(
                "type", request.getType(),
                "boardId", request.getLink().getBoardId(),
                "cardId", request.getLink().getCardId()
        );
        NotificationResponse response = notificationMapper.toNotificationResponse(saved);

        // --- Gửi Firebase Notification ---
        tokenRepository.findByUserId(request.getUserId()).ifPresent(token -> {
            firebaseNotificationService.sendNotification(
                    token.getToken(),
                    request.getTitle(),
                    request.getMessage(),
                    dataPayload
            );
        });
        userRepository.findById(request.getUserId()).ifPresent(user -> {
            WebSocketUpdateResponse wsResponse = WebSocketUpdateResponse.builder()
                    .type(WebSocketUpdateType.NOTIFICATION_CREATED)
                    .payload(response)
                    .build();

            // Gửi đến kênh CÁ NHÂN của user
            // `convertAndSendToUser` sẽ tự động xử lý và gửi đến
            // /user/{user-email}/topic/notifications
            messagingTemplate.convertAndSendToUser(
                    user.getEmail(),          // (Phải là Principal Name, tức là email)
                    "/topic/notifications",   // Tên kênh cá nhân
                    wsResponse
            );
        });
        return response;
    }

    // --- 2. READ (Lấy notifications chưa đọc) ---
    public List<NotificationResponse> getUnreadNotifications(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(notificationMapper::toNotificationResponse)
                .collect(Collectors.toList());
    }

    // --- 3. UPDATE (Đánh dấu đã đọc) ---
    public NotificationResponse markAsRead(String notificationId, NotificationUpdateRequest request,String updatedByUserId) {
        if (!request.getIsRead()) {
            // Không hỗ trợ đánh dấu lại là chưa đọc trong API này
            throw new AppException(ErrorCode.INVALID_OPERATION);
        }

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
        if (!notification.getUserId().equals(updatedByUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        NotificationResponse response;
        if (!notification.isRead()) {
            // Đặt thời gian đọc và trạng thái
            notification.setRead(true);
            notification.setReadAt(Instant.now());
            notification = notificationRepository.save(notification);
            response = notificationMapper.toNotificationResponse(notification);

            // --- (9) GỬI WEBSOCKET (Đồng bộ trạng thái "đã đọc") ---
            userRepository.findById(updatedByUserId).ifPresent(user -> {
                WebSocketUpdateResponse wsResponse = WebSocketUpdateResponse.builder()
                        .type(WebSocketUpdateType.NOTIFICATION_MARKED_AS_READ)
                        .payload(response)
                        .build();

                messagingTemplate.convertAndSendToUser(
                        user.getEmail(),
                        "/topic/notifications",
                        wsResponse
                );
            });
        }
        else {
            response = notificationMapper.toNotificationResponse(notification);
        }

        return response;
    }

    // --- 4. DELETE (Xóa notification) ---
    // Notification thường được xóa tự động bằng TTL Index (Time-To-Live) trong MongoDB sau 90 ngày.
    public void deleteNotification(String notificationId,String deletedByUserId) {
        // Có thể cần kiểm tra quyền trước khi xóa
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // (11) KIỂM TRA QUYỀN
        if (!notification.getUserId().equals(deletedByUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        notificationRepository.deleteById(notificationId);
    }
}
