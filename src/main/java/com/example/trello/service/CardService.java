package com.example.trello.service;

import com.example.trello.dto.request.CardAssigneeRequest;
import com.example.trello.dto.request.CardCreateRequest;
import com.example.trello.dto.request.CardMoveRequest;
import com.example.trello.dto.request.CardUpdateRequest;
import com.example.trello.dto.response.CardResponse;
import com.example.trello.exception.AppException;
import com.example.trello.exception.ErrorCode;
import com.example.trello.mapper.CardMapper;
import com.example.trello.model.Card;
import com.example.trello.repository.CardRepository;
import com.example.trello.repository.ListRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CardService {
    CardRepository cardRepository;
    CardMapper cardMapper;
    ListRepository listRepository;

    // --- 1. CREATE ---
    public CardResponse createCard(CardCreateRequest request) {
        // 1. Tính toán vị trí (position) mới: Lấy max position hiện tại + 1 trong List
        List<Card> cards = cardRepository.findByListIdAndIsArchivedFalseOrderByOrderingAsc(request.getListId());
        double ordering = cards.isEmpty()
                ? 1.0
                : cards.get(cards.size() - 1).getOrdering() + 1.0;

        Card card = cardMapper.toCard(request);
        card.setOrdering(ordering);

        card = cardRepository.save(card);
        // TODO: Ghi Activity log (card_created)
        return cardMapper.toCardResponse(card);
    }

    // --- 2. READ (Single) ---
    public CardResponse getCardById(String cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.CARD_NOT_FOUND));
        return cardMapper.toCardResponse(card);
    }

    // --- 2. READ (All by List) ---
    public List<CardResponse> getCardsByList(String listId) {
        List<Card> cards = cardRepository.findByListIdAndIsArchivedFalseOrderByOrderingAsc(listId);
        return cards.stream()
                .map(cardMapper::toCardResponse)
                .collect(Collectors.toList());
    }

    // --- 3. UPDATE ---
    public CardResponse updateCard(String cardId, CardUpdateRequest request) {
        Card existingCard = cardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.CARD_NOT_FOUND));

        if (request.getIsCompleted() != null && request.getIsCompleted() != existingCard.isCompleted()) {
            existingCard.setCompleted(request.getIsCompleted());
            existingCard.setCompletedAt(request.getIsCompleted() ? Instant.now() : null);
        }

        cardMapper.updateCard(existingCard, request);

        existingCard = cardRepository.save(existingCard);
        return cardMapper.toCardResponse(existingCard);
    }

    // --- 4. DELETE (Archive) ---
    public void archiveCard(String cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.CARD_NOT_FOUND));

        // Soft Delete: Đánh dấu là archive
        card.setArchived(true);
        cardRepository.save(card);

        // TODO: Ghi Activity log (card_archived)
    }
    //BỔ SUNG: Di chuyển thẻ (FR11: Kéo-Thả)
    // --- 5. MOVE (Reorder) ---
    public CardResponse moveCard(String cardId, CardMoveRequest request) {
        Card cardToMove = cardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.CARD_NOT_FOUND));

        // 1. Kiểm tra list đích tồn tại
        listRepository.findById(request.getNewListId())
                .orElseThrow(() -> new AppException(ErrorCode.LIST_NOT_FOUND));

        // 2. Lấy danh sách thẻ trong list đích
        List<Card> cards = cardRepository.findByListIdAndIsArchivedFalseOrderByOrderingAsc(request.getNewListId());
        double newOrdering;

        if (cards.isEmpty()) {
            newOrdering = 1.0;
        } else if (request.getNewPosition() <= 0) {
            newOrdering = cards.get(0).getOrdering() / 2.0;
        } else if (request.getNewPosition() >= cards.size()) {
            newOrdering = cards.get(cards.size() - 1).getOrdering() + 1.0;
        } else {
            double before = cards.get(request.getNewPosition() - 1).getOrdering();
            double after = cards.get(request.getNewPosition()).getOrdering();
            newOrdering = (before + after) / 2.0;
        }

        cardToMove.setListId(request.getNewListId());
        cardToMove.setOrdering(newOrdering);

        Card movedCard = cardRepository.save(cardToMove);
        return cardMapper.toCardResponse(movedCard);
    }

    // --- BỔ SUNG: Gán người dùng vào thẻ (FR12) ---
    public CardResponse assignUser(String cardId, CardAssigneeRequest request) {
        Card existingCard = cardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.CARD_NOT_FOUND));

        // 1. Kiểm tra user đã được gán chưa
        boolean alreadyAssigned = existingCard.getAssignees().stream()
                .anyMatch(assignee -> assignee.getUserId().equals(request.getUserId()));

        if (alreadyAssigned) {
            throw new AppException(ErrorCode.USER_ALREADY_ASSIGNED); // Giả định ErrorCode
        }

        // 2. Tạo đối tượng CardAssignee mới
        Card.CardAssignee newAssignee = Card.CardAssignee.builder()
                .userId(request.getUserId())
                .assignedAt(Instant.now())
                .assignedBy(request.getAssignedBy())
                .build();

        // 3. Thêm vào danh sách
        existingCard.getAssignees().add(newAssignee);

        Card updatedCard = cardRepository.save(existingCard);

        // TODO: Ghi Activity log (user_assigned)
        // TODO: Gửi Notification cho user được gán

        return cardMapper.toCardResponse(updatedCard);
    }

    // --- BỔ SUNG: Hủy gán người dùng ---
    public CardResponse unassignUser(String cardId, String userIdToUnassign) {
        Card existingCard = cardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.CARD_NOT_FOUND));

        // Xóa assignee khỏi danh sách
        boolean removed = existingCard.getAssignees().removeIf(
                assignee -> assignee.getUserId().equals(userIdToUnassign)
        );

        if (!removed) {
            throw new AppException(ErrorCode.USER_NOT_ASSIGNED); // Giả định ErrorCode
        }

        Card updatedCard = cardRepository.save(existingCard);

        // TODO: Ghi Activity log (user_unassigned)

        return cardMapper.toCardResponse(updatedCard);
    }
}