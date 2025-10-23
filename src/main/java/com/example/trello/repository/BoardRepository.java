package com.example.trello.repository;

import com.example.trello.model.Board;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends MongoRepository<Board, String> {
    // FR5-FR7: Lấy tất cả boards của user (owner hoặc member)
    List<Board> findByOwnerIdOrMembers_UserIdAndIsArchivedFalse(String ownerId, String memberId);
}
