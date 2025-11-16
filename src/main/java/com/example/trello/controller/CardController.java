package com.example.trello.controller;

import com.example.trello.dto.request.CardAssigneeRequest;
import com.example.trello.dto.request.CardCreateRequest;
import com.example.trello.dto.request.CardMoveRequest;
import com.example.trello.dto.request.CardUpdateRequest;
import com.example.trello.dto.response.ApiResponse;
import com.example.trello.dto.response.CardResponse;
import com.example.trello.service.CardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CardController {
    CardService cardService;

    // POST: /cards
    @PostMapping
    public ApiResponse<CardResponse> createCard(@RequestBody CardCreateRequest request) {
        return ApiResponse.<CardResponse>builder()
                .data(cardService.createCard(request))
                .build();
    }

    // GET: /cards/{cardId}
    @GetMapping("/{cardId}")
    public ApiResponse<CardResponse> getCard(@PathVariable String cardId) {
        return ApiResponse.<CardResponse>builder()
                .data(cardService.getCardById(cardId))
                .build();
    }

    // GET: /cards/list/{listId} (Lấy tất cả cards trong một list)
    @GetMapping("/list/{listId}")
    public ApiResponse<List<CardResponse>> getCardsByList(@PathVariable String listId) {
        return ApiResponse.<List<CardResponse>>builder()
                .data(cardService.getCardsByList(listId))
                .build();
    }

    // PUT: /cards/{cardId}
    @PutMapping("/{cardId}")
    public ApiResponse<CardResponse> updateCard(
            @PathVariable String cardId,
            @RequestBody CardUpdateRequest request) {
        return ApiResponse.<CardResponse>builder()
                .data(cardService.updateCard(cardId, request))
                .build();
    }

    // DELETE: /cards/{cardId} (Archive Card)
    @DeleteMapping("/{cardId}")
    public ApiResponse<Void> archiveCard(@PathVariable String cardId) {
        cardService.archiveCard(cardId);
        return ApiResponse.<Void>builder()
                .message("Card archived successfully")
                .build();
    }
    // PUT: /cards/{cardId}/move (FR11: Di chuyển thẻ/Kéo thả)
    @PutMapping("/{cardId}/move")
    public ApiResponse<CardResponse> moveCard(
            @PathVariable String cardId,
            @RequestBody CardMoveRequest request) {

        return ApiResponse.<CardResponse>builder()
                .data(cardService.moveCard(cardId, request))
                .build();
    }

    // POST: /cards/{cardId}/assign (FR12: Gán người dùng)
    @PostMapping("/{cardId}/assign")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CardResponse> assignUser(
            @PathVariable String cardId,
            @RequestBody CardAssigneeRequest request) {

        return ApiResponse.<CardResponse>builder()
                .data(cardService.assignUser(cardId, request))
                .build();
    }

    // DELETE: /cards/{cardId}/assign/{userId} (Hủy gán người dùng)
    @DeleteMapping("/{cardId}/assign/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CardResponse> unassignUser(
            @PathVariable String cardId,
            @PathVariable String userId) {

        return ApiResponse.<CardResponse>builder()
                .data(cardService.unassignUser(cardId, userId))
                .build();
    }
    @PutMapping("/{cardId}/attachments")
    public ApiResponse<CardResponse> uploadAttachment(
            @PathVariable String cardId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploadedBy", required = false) String uploadedBy
    ) {
        return ApiResponse.<CardResponse>builder()
                .data(cardService.addAttachment(cardId, file, uploadedBy))
                .build();
    }

}
