package com.example.trello.service;

import com.example.trello.dto.request.*;
import com.example.trello.dto.response.CardResponse;
import com.example.trello.dto.response.WebSocketUpdateResponse;
import com.example.trello.enums.WebSocketUpdateType;
import com.example.trello.exception.AppException;
import com.example.trello.exception.ErrorCode;
import com.example.trello.mapper.CardMapper;
import com.example.trello.model.Card;
import com.example.trello.model.ListEntity;
import com.example.trello.model.Notification;
import com.example.trello.model.User;
import com.example.trello.repository.CardRepository;
import com.example.trello.repository.ListRepository;
import com.example.trello.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CardService {
    CardRepository cardRepository;
    CardMapper cardMapper;
    CloudinaryService cloudinaryService;
    ListRepository listRepository;

    ActivityService activityService;
    NotificationService notificationService;
    UserRepository userRepository;

    SimpMessagingTemplate messagingTemplate;

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
        User creator = userRepository.findById(request.getCreatedBy()).orElse(null);
        ListEntity list = listRepository.findById(request.getListId()).orElse(null);
        if (creator != null && list != null) {
            String logMessage = String.format("%s đã tạo thẻ '%s' trong danh sách '%s'",
                    creator.getDisplayName(),
                    card.getTitle(),
                    list.getTitle()
            );
            activityService.createActivity(ActivityCreateRequest.builder()
                    .boardId(request.getBoardId())
                    .cardId(card.getId())
                    .listId(list.getId())
                    .userId(request.getCreatedBy())
                    .action("create_card")
                    .details(Map.of("text", logMessage))
                    .build());
        }
        broadcastCardUpdate(card, WebSocketUpdateType.CARD_CREATED);
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
    public CardResponse updateCard(String cardId, CardUpdateRequest request,String updatedBy) {
        Card existingCard = cardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.CARD_NOT_FOUND));

        if (request.getIsCompleted() != null && request.getIsCompleted() != existingCard.isCompleted()) {
            existingCard.setCompleted(request.getIsCompleted());
            existingCard.setCompletedAt(request.getIsCompleted() ? Instant.now() : null);
        }

        cardMapper.updateCard(existingCard, request);

        existingCard = cardRepository.save(existingCard);
        broadcastCardUpdate(existingCard, WebSocketUpdateType.CARD_UPDATED);
        return cardMapper.toCardResponse(existingCard);
    }

    // --- 4. DELETE (Archive) ---
    public void archiveCard(String cardId,String userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.CARD_NOT_FOUND));

        // Soft Delete: Đánh dấu là archive
        card.setArchived(true);
        cardRepository.save(card);

        // TODO: Ghi Activity log (card_archived)

        String logMessage = String.format("Thẻ '%s' đã được lưu trữ", card.getTitle());
        activityService.createActivity(ActivityCreateRequest.builder()
                .boardId(card.getBoardId())
                .cardId(cardId)
                .userId(userId)
                .action("archive_card")
                .details(Map.of("text", logMessage))
                .build());
        broadcastCardUpdate(card, WebSocketUpdateType.CARD_UPDATED);
    }
    public CardResponse addAttachment(String cardId, MultipartFile file, String uploadedBy) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.CARD_NOT_FOUND));

        Map<String, Object> uploadResult = cloudinaryService.uploadFile(file, "attachments");

        Card.Attachment attachment = new Card.Attachment();
        attachment.setId(UUID.randomUUID().toString());
        attachment.setName((String) uploadResult.get("original_filename"));
        attachment.setUrl((String) uploadResult.get("secure_url"));
        attachment.setMimeType((String) uploadResult.get("resource_type"));
        attachment.setSize(file.getSize());
        attachment.setUploadedBy(uploadedBy);
        attachment.setUploadedAt(Instant.now());

        if (card.getAttachments() == null) {
            card.setAttachments(new ArrayList<>());
        }
        card.getAttachments().add(attachment);

        cardRepository.save(card);
        return cardMapper.toCardResponse(card);
    }

    //BỔ SUNG: Di chuyển thẻ (FR11: Kéo-Thả)
    // --- 5. MOVE (Reorder) ---
    public CardResponse moveCard(String cardId, CardMoveRequest request) {
        Card cardToMove = cardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.CARD_NOT_FOUND));
        String oldListId = cardToMove.getListId();

        ListEntity newList = listRepository.findById(request.getNewListId())
                .orElseThrow(() -> new AppException(ErrorCode.LIST_NOT_FOUND));
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

        if (!oldListId.equals(request.getNewListId())) {
            ListEntity oldList = listRepository.findById(oldListId)
                    .orElseThrow(() -> new AppException(ErrorCode.LIST_NOT_FOUND));
            User mover = userRepository.findById(request.getMovedBy())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            if (mover != null && oldList != null) {
                String logMessage = String.format("%s đã di chuyển thẻ '%s' từ danh sách '%s' sang '%s'",
                        mover.getDisplayName(),
                        cardToMove.getTitle(),
                        oldList.getTitle(),
                        newList.getTitle()
                );
                activityService.createActivity(ActivityCreateRequest.builder()
                        .boardId(movedCard.getBoardId())
                        .cardId(cardId)
                        .userId(request.getMovedBy())
                        .action("move_card")
                        .details(Map.of(
                                "text", logMessage,
                                "oldListId", oldListId,
                                "newListId", newList.getId()
                        ))
                        .build());
            }
        }
        broadcastCardUpdate(movedCard, WebSocketUpdateType.CARD_UPDATED);
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
        User assigner = userRepository.findById(request.getAssignedBy())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User assignee = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // TODO: Gửi Notification cho user được gán
        if(assigner != null && assignee != null) {
            // --- GHI ACTIVITY LOG (USER_ASSIGNED) ---
            String logMessage = String.format("%s đã gán %s vào thẻ này",
                    assigner.getDisplayName(),
                    assignee.getDisplayName()
            );
            activityService.createActivity(ActivityCreateRequest.builder()
                    .boardId(existingCard.getBoardId())
                    .cardId(cardId)
                    .userId(request.getAssignedBy())
                    .action("assign_user")
                    .details(Map.of("text", logMessage, "assignedUserId", assignee.getId()))
                    .build());

            // --- GỬI NOTIFICATION CHO USER ĐƯỢC GÁN ---
            String title = String.format("%s đã gán bạn vào một thẻ", assigner.getDisplayName());
            String message = String.format("Trong bảng '%s': Thẻ '%s'",
                    "Tên Bảng (Cần query)", // Tạm thời - Bạn cần lấy Board title
                    existingCard.getTitle()
            );
            Notification.NotificationLink link = Notification.NotificationLink.builder()
                    .type("card")
                    .boardId(existingCard.getBoardId())
                    .cardId(cardId)
                    .build();

            notificationService.createNotification(NotificationCreateRequest.builder()
                    .userId(request.getUserId()) // Người nhận
                    .type("card_assigned")
                    .title(title)
                    .message(message)
                    .link(link)
                    .triggeredBy(request.getAssignedBy()) // Người gán
                    .build());
        }
        broadcastCardUpdate(updatedCard, WebSocketUpdateType.CARD_UPDATED);
        return cardMapper.toCardResponse(updatedCard);
    }

    // --- BỔ SUNG: Hủy gán người dùng ---
    public CardResponse unassignUser(String cardId, String userIdToUnassign,String unassignedBy) {
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
        User unassignedUser = userRepository.findById(userIdToUnassign)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if(unassignedUser != null) {
            String logMessage = String.format("%s đã bị hủy gán khỏi thẻ này", unassignedUser.getDisplayName());
            activityService.createActivity(ActivityCreateRequest.builder()
                    .boardId(existingCard.getBoardId())
                    .cardId(cardId)
                    .userId(unassignedBy) // Placeholder - Nên lấy từ Security Principal
                    .action("unassign_user")
                    .details(Map.of("text", logMessage, "unassignedUserId", userIdToUnassign))
                    .build());
        }
        broadcastCardUpdate(updatedCard, WebSocketUpdateType.CARD_UPDATED);
        return cardMapper.toCardResponse(updatedCard);
    }
    /**
     * Hàm nội bộ, chuyên dùng để gửi thông báo WebSocket khi có cập nhật về Card.
     * @param card Đối tượng Card (đã được save)
     * @param type Loại sự kiện (CARD_CREATED, CARD_UPDATED, v.v.)
     */
    private void broadcastCardUpdate(Card card, WebSocketUpdateType type) {
        // 1. Map sang DTO
        CardResponse cardResponse = cardMapper.toCardResponse(card);

        // 2. Build message
        WebSocketUpdateResponse wsResponse = WebSocketUpdateResponse.builder()
                .type(type)
                .payload(cardResponse)
                .build();

        // 3. Gửi (Sửa lại tên topic cho nhất quán)
        messagingTemplate.convertAndSend("/topic/board/" + card.getBoardId(), wsResponse);
    }
}