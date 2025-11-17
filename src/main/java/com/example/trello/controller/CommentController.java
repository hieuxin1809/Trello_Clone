package com.example.trello.controller;

import com.example.trello.dto.request.CommentCreateRequest;
import com.example.trello.dto.request.CommentUpdateRequest;
import com.example.trello.dto.response.ApiResponse;
import com.example.trello.dto.response.CommentResponse;
import com.example.trello.model.User;
import com.example.trello.service.CommentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentController {
    CommentService commentService;

    // POST: /comments
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@boardSecurityService.isAtLeastMember(#request.boardId, principal)")
    public ApiResponse<CommentResponse> createComment(
            @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal User principal) {
        request.setUserId(principal.getId());
        return ApiResponse.<CommentResponse>builder()
                .data(commentService.createComment(request))
                .build();
    }

    // GET: /comments/card/{cardId} (Lấy tất cả comments của một thẻ)
    @GetMapping("/card/{cardId}")
    @PreAuthorize("@boardSecurityService.isMemberOfCard(#cardId, principal)")
    public ApiResponse<List<CommentResponse>> getCommentsByCard(
            @PathVariable String cardId,
            @AuthenticationPrincipal User principal) {
        return ApiResponse.<List<CommentResponse>>builder()
                .data(commentService.getCommentsByCard(cardId))
                .build();
    }

    // PUT: /comments/{commentId}
    @PutMapping("/{commentId}")
    @PreAuthorize("@commentSecurityService.isAuthor(#commentId, principal)")
    public ApiResponse<CommentResponse> updateComment(
            @PathVariable String commentId,
            @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal User principal ) {
        return ApiResponse.<CommentResponse>builder()
                .data(commentService.updateComment(commentId, request,principal.getId()))
                .build();
    }

    // DELETE: /comments/{commentId}
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@commentSecurityService.isAuthorOrManager(#commentId, user)")
    public ApiResponse<Void> deleteComment(
            @PathVariable String commentId,
            @AuthenticationPrincipal User user) {
        commentService.deleteComment(commentId,user.getId());
        return ApiResponse.<Void>builder()
                .message("Comment deleted successfully")
                .build();
    }
}
