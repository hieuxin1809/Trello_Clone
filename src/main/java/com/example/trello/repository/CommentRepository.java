package com.example.trello.repository;

import com.example.trello.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {
    // Lấy tất cả comments của một card, sắp xếp theo thời gian tạo mới nhất
    List<Comment> findByCardIdOrderByCreatedAtDesc(String cardId);
}
