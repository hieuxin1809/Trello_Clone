package com.example.trello.controller;

import com.example.trello.dto.response.ApiResponse;
import com.example.trello.dto.response.ActivityResponse;
import com.example.trello.service.ActivityService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivityController {
    ActivityService activityService;

    // GET: /activities/board/{boardId} (Lấy lịch sử hoạt động của Board)
    @GetMapping("/board/{boardId}")
    public ApiResponse<List<ActivityResponse>> getActivitiesByBoard(@PathVariable String boardId) {
        return ApiResponse.<List<ActivityResponse>>builder()
                .data(activityService.getActivitiesByBoard(boardId))
                .build();
    }

    // GET: /activities/card/{cardId} (Lấy lịch sử hoạt động của Card)
    @GetMapping("/card/{cardId}")
    public ApiResponse<List<ActivityResponse>> getActivitiesByCard(@PathVariable String cardId) {
        return ApiResponse.<List<ActivityResponse>>builder()
                .data(activityService.getActivitiesByCard(cardId))
                .build();
    }
}