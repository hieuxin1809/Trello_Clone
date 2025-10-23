package com.example.trello.mapper;

import com.example.trello.dto.request.NotificationCreateRequest;
import com.example.trello.dto.request.NotificationUpdateRequest;
import com.example.trello.dto.response.NotificationResponse;
import com.example.trello.model.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;

@Mapper(componentModel = "spring", imports = {Instant.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    // --- CREATE Mapping (Hệ thống gọi) ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isRead", constant = "false")
    @Mapping(target = "emailSent", constant = "false") // Giả định chưa gửi email
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    // Note: readAt và emailSentAt sẽ được đặt trong Service
    Notification toNotification(NotificationCreateRequest request);

    // --- READ Mapping ---
    NotificationResponse toNotificationResponse(Notification notification);

    // --- UPDATE Mapping (Dùng cho việc đánh dấu đã đọc) ---
    @Mapping(target = "read", source = "isRead")
    void updateNotification(@MappingTarget Notification notification, NotificationUpdateRequest request);
}