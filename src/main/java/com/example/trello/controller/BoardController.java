package com.example.trello.controller;

import com.example.trello.dto.request.BoardCreateRequest;
import com.example.trello.dto.request.BoardMemberRequest;
import com.example.trello.dto.request.BoardUpdateRequest;
import com.example.trello.dto.response.ApiResponse;
import com.example.trello.dto.response.BoardResponse;
import com.example.trello.model.User;
import com.example.trello.service.BoardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BoardController {
    BoardService boardService;

    // POST: /boards
    @PostMapping
    public ApiResponse<BoardResponse> createBoard(@RequestBody BoardCreateRequest request) {
        return ApiResponse.<BoardResponse>builder()
                .data(boardService.createBoard(request))
                .build();
    }

    // GET: /boards/{boardId}
    @GetMapping("/{boardId}")
    public ApiResponse<BoardResponse> getBoard(@PathVariable String boardId) {
        return ApiResponse.<BoardResponse>builder()
                .data(boardService.getBoardById(boardId))
                .build();
    }
    // GET: /boards/user/{userId}
    @GetMapping("/user/{userId}")
    public ApiResponse<List<BoardResponse>> getBoardsOfUser(@PathVariable String userId) {
        return ApiResponse.<List<BoardResponse>>builder()
                .data(boardService.getBoardsByUserId(userId))
                .build();
    }

    // PUT: /boards/{boardId}
    @PutMapping("/{boardId}")
    @PreAuthorize("@boardSecurityService.isAtLeastManager(#boardId, user)")
    public ApiResponse<BoardResponse> updateBoard(
            @PathVariable String boardId,
            @RequestBody BoardUpdateRequest request,
            @AuthenticationPrincipal User user) {
        return ApiResponse.<BoardResponse>builder()
                .data(boardService.updateBoard(boardId, request,user.getId()))
                .build();
    }

    // DELETE: /boards/{boardId}
    @DeleteMapping("/{boardId}")
    @PreAuthorize("@boardSecurityService.isAtLeastManager(#boardId, user)")
    public ApiResponse<Void> closeBoard(@PathVariable String boardId
            ,@AuthenticationPrincipal User user) {
        boardService.closeBoard(boardId, user.getId());
        return ApiResponse.<Void>builder()
                .message("Board closed successfully (Soft Delete/Archive)")
                .build();
    }
    // POST: /boards/{boardId}/members (FR7: Thêm thành viên)
    @PostMapping("/{boardId}/members")
    @PreAuthorize("@boardSecurityService.isAtLeastManager(#boardId, principal)")
    public ApiResponse<BoardResponse> addMember(
            @PathVariable String boardId,
            @RequestBody BoardMemberRequest request,
            @AuthenticationPrincipal User principal) {
        request.setAddedBy(principal.getId());

        return ApiResponse.<BoardResponse>builder()
                .data(boardService.addMember(boardId, request))
                .build();
    }

    // DELETE: /boards/{boardId}/members/{userId} (FR7: Xóa thành viên)
    @DeleteMapping("/{boardId}/members/{userId}")
    @PreAuthorize("@boardSecurityService.isAtLeastManager(#boardId, currentUser)")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content
    public ApiResponse<Void> removeMember(
            @PathVariable String boardId,
            @PathVariable String userId,
            @AuthenticationPrincipal User currentUser) {

        boardService.removeMember(boardId, userId, currentUser.getId());
        return ApiResponse.<Void>builder()
                .message("Member removed successfully")
                .build();
    }
}
