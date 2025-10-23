package com.example.trello.mapper;

import com.example.trello.dto.request.LabelCreateRequest;
import com.example.trello.dto.request.LabelUpdateRequest;
import com.example.trello.dto.response.LabelResponse;
import com.example.trello.model.Label;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.time.Instant; // Sửa sang Instant

@Mapper(componentModel = "spring", imports = {Instant.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LabelMapper {

    // --- CREATE Mapping ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(Instant.now())") // Dùng Instant
    Label toLabel(LabelCreateRequest request);

    // --- READ Mapping ---
    LabelResponse toLabelResponse(Label label);

    // --- UPDATE Mapping ---
    void updateLabel(@MappingTarget Label label, LabelUpdateRequest request);
}
