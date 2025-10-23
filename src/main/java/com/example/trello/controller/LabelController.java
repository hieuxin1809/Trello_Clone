package com.example.trello.controller;

import com.example.trello.dto.request.LabelCreateRequest;
import com.example.trello.dto.request.LabelUpdateRequest;
import com.example.trello.dto.response.ApiResponse;
import com.example.trello.dto.response.LabelResponse;
import com.example.trello.service.LabelService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/labels")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LabelController {
    LabelService labelService;

    // POST: /labels
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LabelResponse> createLabel(@RequestBody LabelCreateRequest request) {
        return ApiResponse.<LabelResponse>builder()
                .data(labelService.createLabel(request))
                .build();
    }

    // GET: /labels/{labelId}
    @GetMapping("/{labelId}")
    public ApiResponse<LabelResponse> getLabel(@PathVariable String labelId) {
        return ApiResponse.<LabelResponse>builder()
                .data(labelService.getLabelById(labelId))
                .build();
    }

    // GET: /labels/board/{boardId}
    @GetMapping("/board/{boardId}")
    public ApiResponse<List<LabelResponse>> getLabelsByBoard(@PathVariable String boardId) {
        return ApiResponse.<List<LabelResponse>>builder()
                .data(labelService.getLabelsByBoard(boardId))
                .build();
    }

    // PUT: /labels/{labelId}
    @PutMapping("/{labelId}")
    public ApiResponse<LabelResponse> updateLabel(
            @PathVariable String labelId,
            @RequestBody LabelUpdateRequest request) {
        return ApiResponse.<LabelResponse>builder()
                .data(labelService.updateLabel(labelId, request))
                .build();
    }

    // DELETE: /labels/{labelId}
    @DeleteMapping("/{labelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteLabel(@PathVariable String labelId) {
        labelService.deleteLabel(labelId);
        return ApiResponse.<Void>builder()
                .message("Label deleted successfully")
                .build();
    }
}
