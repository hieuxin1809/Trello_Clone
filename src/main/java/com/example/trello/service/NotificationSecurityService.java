package com.example.trello.service;

import com.example.trello.exception.AppException;
import com.example.trello.exception.ErrorCode;
import com.example.trello.model.Notification;
import com.example.trello.model.User;
import com.example.trello.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("notificationSecurityService") // Đặt tên cho bean
@RequiredArgsConstructor
public class NotificationSecurityService {

    private final NotificationRepository notificationRepository;
    /**
     * Kiểm tra xem user có phải là CHỦ SỞ HỮU của notification không
     */
    public boolean isOwner(String notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
        // So sánh userId trên notification với ID của người đang đăng nhập
        return notification.getUserId().equals(user.getId());
    }
}