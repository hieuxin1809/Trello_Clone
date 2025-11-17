package com.example.trello.controller;

import com.example.trello.dto.request.CardAssigneeRequest;
import com.example.trello.dto.request.CardCreateRequest;
import com.example.trello.dto.request.CardMoveRequest;
import com.example.trello.dto.request.CardUpdateRequest;
import com.example.trello.dto.response.ApiResponse;
import com.example.trello.dto.response.CardResponse;
import com.example.trello.model.User;
import com.example.trello.service.CardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PreAuthorize("@boardSecurityService.isAtLeastMember(#request.boardId, principal)")
    public ApiResponse<CardResponse> createCard(
            @RequestBody CardCreateRequest request,
            @AuthenticationPrincipal User principal) {
        request.setCreatedBy(principal.getId());
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
    @PreAuthorize("@boardSecurityService.isMemberOfCard(#cardId, principal)")
    public ApiResponse<CardResponse> updateCard(
            @PathVariable String cardId,
            @RequestBody CardUpdateRequest request,
            @AuthenticationPrincipal User principal) {
        return ApiResponse.<CardResponse>builder()
                .data(cardService.updateCard(cardId, request,principal.getId()))
                .build();
    }

    // DELETE: /cards/{cardId} (Archive Card)
    @DeleteMapping("/{cardId}")
    public ApiResponse<Void> archiveCard(@PathVariable String cardId,@AuthenticationPrincipal User user) {
        cardService.archiveCard(cardId,user.getId());
        return ApiResponse.<Void>builder()
                .message("Card archived successfully")
                .build();
    }
    // PUT: /cards/{cardId}/move (FR11: Di chuyển thẻ/Kéo thả)
    @PutMapping("/{cardId}/move")
    @PreAuthorize("@boardSecurityService.isMemberOfCard(#cardId, principal)")
    public ApiResponse<CardResponse> moveCard(
            @PathVariable String cardId,
            @RequestBody CardMoveRequest request,
            @AuthenticationPrincipal User principal) {
        request.setMovedBy(principal.getId());
        return ApiResponse.<CardResponse>builder()
                .data(cardService.moveCard(cardId, request))
                .build();
    }

    // POST: /cards/{cardId}/assign (FR12: Gán người dùng)
    @PostMapping("/{cardId}/assign")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@boardSecurityService.isManagerOfCard(cardId, principal)")
    public ApiResponse<CardResponse> assignUser(
            @PathVariable String cardId,
            @RequestBody CardAssigneeRequest request,
            @AuthenticationPrincipal User principal) {
        request.setAssignedBy(principal.getId());
        return ApiResponse.<CardResponse>builder()
                .data(cardService.assignUser(cardId, request))
                .build();
    }

    // DELETE: /cards/{cardId}/assign/{userId} (Hủy gán người dùng)
    @DeleteMapping("/{cardId}/assign/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@boardSecurityService.isMemberOfCard(#cardId, user)")
    public ApiResponse<CardResponse> unassignUser(
            @PathVariable String cardId,
            @PathVariable String userId,
            @AuthenticationPrincipal User user) {

        return ApiResponse.<CardResponse>builder()
                .data(cardService.unassignUser(cardId, userId,user.getId()))
                .build();
    }
    @PutMapping("/{cardId}/attachments")
    @PreAuthorize("@boardSecurityService.isMemberOfCard(#cardId, principal)")
    public ApiResponse<CardResponse> uploadAttachment(
            @PathVariable String cardId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User principal
    ) {
        return ApiResponse.<CardResponse>builder()
                .data(cardService.addAttachment(cardId, file, principal.getId()))
                .build();
    }

}
