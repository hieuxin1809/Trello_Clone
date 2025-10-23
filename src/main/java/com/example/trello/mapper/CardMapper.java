package com.example.trello.mapper;

import com.example.trello.dto.request.CardCreateRequest;
import com.example.trello.dto.request.CardUpdateRequest;
import com.example.trello.dto.response.CardResponse;
import com.example.trello.model.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;

@Mapper(componentModel = "spring", imports = {Instant.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CardMapper {

    // --- CREATE Mapping ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ordering", constant = "-1") // Vị trí sẽ được tính toán trong Service
    @Mapping(target = "isArchived", constant = "false")
    @Mapping(target = "isCompleted", constant = "false")
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    @Mapping(target = "updatedAt", expression = "java(Instant.now())")
    Card toCard(CardCreateRequest request);

    // --- READ Mapping ---
    CardResponse toCardResponse(Card card);

    // --- UPDATE Mapping ---
    @Mapping(target = "updatedAt", expression = "java(Instant.now())")
    // Note: Logic phức tạp (như position, listId, isCompleted) sẽ được xử lý trong Service
    void updateCard(@MappingTarget Card card, CardUpdateRequest request);
}