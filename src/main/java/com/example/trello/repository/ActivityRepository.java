package com.example.trello.repository;

import com.example.trello.model.Activity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends MongoRepository<Activity, String> {
    // Lấy log theo Board, sắp xếp theo thời gian mới nhất
    List<Activity> findByBoardIdOrderByCreatedAtDesc(String boardId);

    // Lấy log theo Card
    List<Activity> findByCardIdOrderByCreatedAtDesc(String cardId);
}