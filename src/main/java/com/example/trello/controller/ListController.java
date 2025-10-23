package com.example.trello.controller;

import com.example.trello.dto.request.ListCreateRequest;
import com.example.trello.dto.request.ListUpdateRequest;
import com.example.trello.dto.response.ApiResponse;
import com.example.trello.dto.response.ListResponse;
import com.example.trello.service.ListService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lists")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ListController {
    ListService listService;

    // POST: /lists
    @PostMapping
    public ApiResponse<ListResponse> createList(@RequestBody ListCreateRequest request) {
        return ApiResponse.<ListResponse>builder()
                .data(listService.createList(request))
                .build();
    }

    // GET: /lists/{listId}
    @GetMapping("/{listId}")
    public ApiResponse<ListResponse> getList(@PathVariable String listId) {
        return ApiResponse.<ListResponse>builder()
                .data(listService.getListById(listId))
                .build();
    }

    // GET: /lists/board/{boardId}
    @GetMapping("/board/{boardId}")
    public ApiResponse<List<ListResponse>> getListsByBoard(@PathVariable String boardId) {
        return ApiResponse.<List<ListResponse>>builder()
                .data(listService.getListsByBoard(boardId))
                .build();
    }

    // PUT: /lists/{listId}
    @PutMapping("/{listId}")
    public ApiResponse<ListResponse> updateList(
            @PathVariable String listId,
            @RequestBody ListUpdateRequest request) {
        return ApiResponse.<ListResponse>builder()
                .data(listService.updateList(listId, request))
                .build();
    }

    // DELETE: /lists/{listId}
    @DeleteMapping("/{listId}")
    public ApiResponse<Void> archiveList(@PathVariable String listId) {
        listService.archiveList(listId);
        return ApiResponse.<Void>builder()
                .message("List archived successfully")
                .build();
    }
}
