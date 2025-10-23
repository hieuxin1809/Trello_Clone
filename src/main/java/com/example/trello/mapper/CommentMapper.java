package com.example.trello.mapper;

import com.example.trello.dto.request.CommentCreateRequest;
import com.example.trello.dto.request.CommentUpdateRequest;
import com.example.trello.dto.response.CommentResponse;
import com.example.trello.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;

@Mapper(componentModel = "spring", imports = {Instant.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

    // --- CREATE Mapping ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isEdited", constant = "false")
    @Mapping(target = "editedAt", ignore = true)
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(Instant.now())")
    Comment toComment(CommentCreateRequest request);

    // --- READ Mapping ---
    CommentResponse toCommentResponse(Comment comment);

    // --- UPDATE Mapping ---
    @Mapping(target = "updatedAt", expression = "java(Instant.now())")
    // Note: isEdited và editedAt sẽ được xử lý trong Service
    void updateComment(@MappingTarget Comment comment, CommentUpdateRequest request);
}