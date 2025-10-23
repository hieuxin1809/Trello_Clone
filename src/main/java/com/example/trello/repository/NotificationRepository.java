package com.example.trello.repository;

import com.example.trello.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    //Lấy notifications chưa đọc của user
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(String userId);

    // Lấy tất cả notifications của user
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
}