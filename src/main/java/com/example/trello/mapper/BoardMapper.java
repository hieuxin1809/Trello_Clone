package com.example.trello.mapper;

import com.example.trello.dto.request.BoardCreateRequest;
import com.example.trello.dto.request.BoardUpdateRequest;
import com.example.trello.dto.response.BoardResponse;
import com.example.trello.model.Board;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;

@Mapper(componentModel = "spring", imports = {Instant.class, Board.Visibility.class, Board.BoardMember.class, Board.BoardMember.MemberRole.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BoardMapper {

    // --- CREATE Mapping ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "visibility", expression = "java(Board.Visibility.valueOf(request.getVisibility().toUpperCase()))")
    @Mapping(target = "isClosed", constant = "false")
    @Mapping(target = "isArchived", constant = "false")
    @Mapping(target = "createdAt", expression = "java(Instant.now())") // Dùng Instant
    @Mapping(target = "updatedAt", expression = "java(Instant.now())") // Dùng Instant
    Board toBoard(BoardCreateRequest request);

    // --- READ Mapping ---
    @Mapping(target = "visibility", expression = "java(board.getVisibility().name())")
    BoardResponse toBoardResponse(Board board);

    // --- UPDATE Mapping ---
    @Mapping(target = "updatedAt", expression = "java(Instant.now())") // Dùng Instant
    @Mapping(target = "visibility", expression = "java(request.getVisibility() != null ? Board.Visibility.valueOf(request.getVisibility().toUpperCase()) : board.getVisibility())")
    void updateBoard(@MappingTarget Board board, BoardUpdateRequest request);
}
