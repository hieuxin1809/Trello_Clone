package com.example.trello.controller;

import com.example.trello.dto.request.NotificationCreateRequest;
import com.example.trello.dto.request.NotificationUpdateRequest;
import com.example.trello.dto.response.ApiResponse;
import com.example.trello.dto.response.NotificationResponse;
import com.example.trello.model.User;
import com.example.trello.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {
    NotificationService notificationService;

    // POST: /notifications (Chỉ dùng để kiểm tra, thường do hệ thống gọi)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<NotificationResponse> createNotification(@RequestBody NotificationCreateRequest request) {
        return ApiResponse.<NotificationResponse>builder()
                .data(notificationService.createNotification(request))
                .build();
    }

    // GET: /notifications/user/{userId} (Lấy notifications chưa đọc)
    @GetMapping("/me")
    public ApiResponse<List<NotificationResponse>> getUnreadNotifications(
            @AuthenticationPrincipal User currentUser) {
        return ApiResponse.<List<NotificationResponse>>builder()
                .data(notificationService.getUnreadNotifications(currentUser.getId()))
                .build();
    }

    // PUT: /notifications/{notificationId} (Đánh dấu đã đọc)
    @PutMapping("/{notificationId}")
    @PreAuthorize("@notificationSecurityService.isOwner(#notificationId, user)")
    public ApiResponse<NotificationResponse> markAsRead(
            @PathVariable String notificationId,
            @RequestBody NotificationUpdateRequest request,
            @AuthenticationPrincipal User user) {
        return ApiResponse.<NotificationResponse>builder()
                .data(notificationService.markAsRead(notificationId, request,user.getId()))
                .build();
    }

    // DELETE: /notifications/{notificationId}
    @DeleteMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@notificationSecurityService.isOwner(#notificationId, user)")
    public ApiResponse<Void> deleteNotification(@PathVariable String notificationId,
                                                @AuthenticationPrincipal User user) {
        notificationService.deleteNotification(notificationId, user.getId());
        return ApiResponse.<Void>builder()
                .message("Notification deleted successfully")
                .build();
    }
}