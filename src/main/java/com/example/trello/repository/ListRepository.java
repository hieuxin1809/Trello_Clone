package com.example.trello.repository;

import com.example.trello.model.ListEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListRepository extends MongoRepository<ListEntity, String> {
    List<ListEntity> findByBoardIdAndIsArchivedFalseOrderByPositionAsc(String boardId);
}
