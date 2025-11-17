package com.example.trello.controller;

import com.example.trello.dto.response.ApiResponse;
import com.example.trello.dto.response.ActivityResponse;
import com.example.trello.model.User;
import com.example.trello.service.ActivityService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivityController {
    ActivityService activityService;

    // GET: /activities/board/{boardId} (Lấy lịch sử hoạt động của Board)
    @PreAuthorize("hasRole('ADMIN') or @boardSecurityService.isMember(#boardId, principal)")
    @GetMapping("/board/{boardId}")
    public ApiResponse<List<ActivityResponse>> getActivitiesByBoard(
            @PathVariable String boardId,
            @AuthenticationPrincipal User principal) {
        return ApiResponse.<List<ActivityResponse>>builder()
                .data(activityService.getActivitiesByBoard(boardId))
                .build();
    }

    // GET: /activities/card/{cardId} (Lấy lịch sử hoạt động của Card)
    @GetMapping("/card/{cardId}")
    @PreAuthorize("hasRole('ADMIN') or @boardSecurityService.isMemberOfCard(#cardId, principal)")
    public ApiResponse<List<ActivityResponse>> getActivitiesByCard(
            @PathVariable String cardId,
            @AuthenticationPrincipal User principal) {
        return ApiResponse.<List<ActivityResponse>>builder()
                .data(activityService.getActivitiesByCard(cardId))
                .build();
    }
}