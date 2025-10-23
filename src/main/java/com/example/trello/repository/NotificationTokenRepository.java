package com.example.trello.repository;

import com.example.trello.model.NotificationToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface NotificationTokenRepository extends MongoRepository<NotificationToken, String> {
    Optional<NotificationToken> findByUserId(String userId);
}
