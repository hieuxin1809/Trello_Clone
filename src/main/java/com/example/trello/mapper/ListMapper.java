package com.example.trello.mapper;

import com.example.trello.dto.request.ListCreateRequest;
import com.example.trello.dto.request.ListUpdateRequest;
import com.example.trello.dto.response.ListResponse;
import com.example.trello.model.ListEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;

@Mapper(componentModel = "spring", imports = {Instant.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ListMapper {

    // --- CREATE Mapping ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isArchived", constant = "false")
    @Mapping(target = "position", constant = "-1")
    @Mapping(target = "createdAt", expression = "java(Instant.now())") // Dùng Instant
    @Mapping(target = "updatedAt", expression = "java(Instant.now())")
    // Dùng Instant
    ListEntity toList(ListCreateRequest request);

    // --- READ Mapping ---
    ListResponse toListResponse(ListEntity listEntity);

    // --- UPDATE Mapping ---
    @Mapping(target = "updatedAt", expression = "java(Instant.now())")
    // Dùng Instant
    void updateList(@MappingTarget ListEntity listEntity, ListUpdateRequest request);
}
