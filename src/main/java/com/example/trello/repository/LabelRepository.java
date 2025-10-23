package com.example.trello.repository;
import com.example.trello.model.Label;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabelRepository extends MongoRepository<Label, String> {
    List<Label> findByBoardId(String boardId);
}
