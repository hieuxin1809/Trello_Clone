package com.example.trello.controller;

import com.example.trello.dto.request.ListCreateRequest;
import com.example.trello.dto.request.ListUpdateRequest;
import com.example.trello.dto.response.ApiResponse;
import com.example.trello.dto.response.ListResponse;
import com.example.trello.model.User;
import com.example.trello.service.ListService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PreAuthorize("@boardSecurityService.isAtLeastManager(#request.boardId, principal)")
    public ApiResponse<ListResponse> createList(
            @RequestBody ListCreateRequest request,
            @AuthenticationPrincipal User principal
    ) {
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
    @PreAuthorize("@boardSecurityService.isAtLeastMember(#boardId, principal)")
    public ApiResponse<List<ListResponse>> getListsByBoard(
            @PathVariable String boardId,
            @AuthenticationPrincipal User principal
    ) {
        return ApiResponse.<List<ListResponse>>builder()
                .data(listService.getListsByBoard(boardId))
                .build();
    }

    // PUT: /lists/{listId}
    @PutMapping("/{listId}")
    @PreAuthorize("@boardSecurityService.isManagerOfList(#listId, user)")
    public ApiResponse<ListResponse> updateList(
            @PathVariable String listId,
            @RequestBody ListUpdateRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ApiResponse.<ListResponse>builder()
                .data(listService.updateList(listId, request, user.getId()))
                .build();
    }

    // DELETE: /lists/{listId}
    @DeleteMapping("/{listId}")
    @PreAuthorize("@boardSecurityService.isManagerOfList(#listId, user)")
    public ApiResponse<Void> archiveList(
            @PathVariable String listId,
            @AuthenticationPrincipal User user) {
        listService.archiveList(listId,user.getId());
        return ApiResponse.<Void>builder()
                .message("List archived successfully")
                .build();
    }
}
