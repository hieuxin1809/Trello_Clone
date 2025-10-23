package com.example.trello.repository;

import com.example.trello.model.Card;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends MongoRepository<Card, String> {
    // Dùng để tìm cards trong một list, chưa archive, sắp xếp theo position
    List<Card> findByListIdAndIsArchivedFalseOrderByOrderingAsc(String listId);

    // Dùng cho thống kê hoặc chức năng khác
    List<Card> findByBoardId(String boardId);

    List<Card> findByListIdOrderByOrderingAsc(String listId);

}