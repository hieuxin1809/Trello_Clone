package com.example.trello.mapper;

import com.example.trello.dto.request.ActivityCreateRequest;
import com.example.trello.dto.response.ActivityResponse;
import com.example.trello.model.Activity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;

@Mapper(componentModel = "spring", imports = {Instant.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ActivityMapper {

    // --- CREATE Mapping (Hệ thống gọi) ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    Activity toActivity(ActivityCreateRequest request);

    // --- READ Mapping ---
    ActivityResponse toActivityResponse(Activity activity);
}