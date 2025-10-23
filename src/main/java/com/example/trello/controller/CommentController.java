package com.example.trello.controller;

import com.example.trello.dto.request.CommentCreateRequest;
import com.example.trello.dto.request.CommentUpdateRequest;
import com.example.trello.dto.response.ApiResponse;
import com.example.trello.dto.response.CommentResponse;
import com.example.trello.service.CommentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
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
    public ApiResponse<CommentResponse> createComment(@RequestBody CommentCreateRequest request) {
        return ApiResponse.<CommentResponse>builder()
                .data(commentService.createComment(request))
                .build();
    }

    // GET: /comments/card/{cardId} (Lấy tất cả comments của một thẻ)
    @GetMapping("/card/{cardId}")
    public ApiResponse<List<CommentResponse>> getCommentsByCard(@PathVariable String cardId) {
        return ApiResponse.<List<CommentResponse>>builder()
                .data(commentService.getCommentsByCard(cardId))
                .build();
    }

    // PUT: /comments/{commentId}
    @PutMapping("/{commentId}")
    public ApiResponse<CommentResponse> updateComment(
            @PathVariable String commentId,
            @RequestBody CommentUpdateRequest request) {
        return ApiResponse.<CommentResponse>builder()
                .data(commentService.updateComment(commentId, request))
                .build();
    }

    // DELETE: /comments/{commentId}
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteComment(@PathVariable String commentId) {
        commentService.deleteComment(commentId);
        return ApiResponse.<Void>builder()
                .message("Comment deleted successfully")
                .build();
    }
}
