package com.example.trello.service;

import com.example.trello.dto.request.NotificationCreateRequest;
import com.example.trello.dto.request.NotificationUpdateRequest;
import com.example.trello.dto.response.NotificationResponse;
import com.example.trello.exception.AppException;
import com.example.trello.exception.ErrorCode;
import com.example.trello.mapper.NotificationMapper;
import com.example.trello.model.Notification;
import com.example.trello.repository.NotificationRepository;
import com.example.trello.repository.NotificationTokenRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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

    // --- 1. CREATE (Được gọi nội bộ khi có sự kiện: comment, assign, due date,...) ---
    public NotificationResponse createNotification(NotificationCreateRequest request) {
        Notification notification = notificationMapper.toNotification(request);
        Notification saved = notificationRepository.save(notification);
        Map<String, String> dataPayload = Map.of(
                "type", request.getType(),
                "boardId", request.getLink().getBoardId(),
                "cardId", request.getLink().getCardId()
        );

        // --- Gửi Firebase Notification ---
        tokenRepository.findByUserId(request.getUserId()).ifPresent(token -> {
            firebaseNotificationService.sendNotification(
                    token.getToken(),
                    request.getTitle(),
                    request.getMessage(),
                    dataPayload
            );
        });

        return notificationMapper.toNotificationResponse(saved);
    }

    // --- 2. READ (Lấy notifications chưa đọc) ---
    public List<NotificationResponse> getUnreadNotifications(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(notificationMapper::toNotificationResponse)
                .collect(Collectors.toList());
    }

    // --- 3. UPDATE (Đánh dấu đã đọc) ---
    public NotificationResponse markAsRead(String notificationId, NotificationUpdateRequest request) {
        if (!request.getIsRead()) {
            // Không hỗ trợ đánh dấu lại là chưa đọc trong API này
            throw new AppException(ErrorCode.INVALID_OPERATION);
        }

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.isRead()) {
            // Đặt thời gian đọc và trạng thái
            notification.setRead(true);
            notification.setReadAt(Instant.now());
            notification = notificationRepository.save(notification);
        }

        return notificationMapper.toNotificationResponse(notification);
    }

    // --- 4. DELETE (Xóa notification) ---
    // Notification thường được xóa tự động bằng TTL Index (Time-To-Live) trong MongoDB sau 90 ngày.
    public void deleteNotification(String notificationId) {
        // Có thể cần kiểm tra quyền trước khi xóa
        notificationRepository.deleteById(notificationId);
    }
}
