package com.example.trello.service;

import com.example.trello.dto.request.ActivityCreateRequest;
import com.example.trello.dto.request.LabelCreateRequest;
import com.example.trello.dto.request.LabelUpdateRequest;
import com.example.trello.dto.response.LabelResponse;
import com.example.trello.dto.response.WebSocketUpdateResponse;
import com.example.trello.enums.WebSocketUpdateType;
import com.example.trello.exception.AppException;
import com.example.trello.exception.ErrorCode;
import com.example.trello.mapper.LabelMapper;
import com.example.trello.model.Label;
import com.example.trello.model.User;
import com.example.trello.repository.LabelRepository;
import com.example.trello.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LabelService {
    LabelRepository labelRepository;
    LabelMapper labelMapper;
    ActivityService activityService;
    UserRepository userRepository;
    SimpMessagingTemplate messagingTemplate;

    // --- 1. CREATE ---
    public LabelResponse createLabel(LabelCreateRequest request) {
        // TODO: Kiểm tra xem tên nhãn đã tồn tại trong Board chưa
        Label label = labelMapper.toLabel(request);
        User creator = userRepository.findById(request.getCreatedBy()).orElse(null);
        if (creator != null) {
            String logMessage = String.format("%s đã tạo nhãn '%s' (màu %s)",
                    creator.getDisplayName(), label.getName(), label.getColor());
            activityService.createActivity(ActivityCreateRequest.builder()
                    .boardId(request.getBoardId())
                    .userId(request.getCreatedBy())
                    .action("create_label")
                    .details(Map.of("text", logMessage, "labelId", label.getId()))
                    .build());
        }
        return labelMapper.toLabelResponse(labelRepository.save(label));
    }

    // --- 2. READ (Single) ---
    public LabelResponse getLabelById(String labelId) {
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new AppException(ErrorCode.LABEL_NOT_FOUND));
        return labelMapper.toLabelResponse(label);
    }

    // --- 2. READ (All by Board) ---
    public List<LabelResponse> getLabelsByBoard(String boardId) {
        List<Label> labels = labelRepository.findByBoardId(boardId);
        return labels.stream()
                .map(labelMapper::toLabelResponse)
                .collect(Collectors.toList());
    }

    // --- 3. UPDATE ---
    public LabelResponse updateLabel(String labelId, LabelUpdateRequest request,String updatedByUserId) {
        Label existingLabel = labelRepository.findById(labelId)
                .orElseThrow(() -> new AppException(ErrorCode.LABEL_NOT_FOUND));

        labelMapper.updateLabel(existingLabel, request);

        existingLabel = labelRepository.save(existingLabel);
        User updater = userRepository.findById(updatedByUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (updater != null) {
            String logMessage = String.format("%s đã cập nhật nhãn '%s'",
                    updater.getDisplayName(), existingLabel.getName());
            activityService.createActivity(ActivityCreateRequest.builder()
                    .boardId(existingLabel.getBoardId())
                    .userId(updatedByUserId)
                    .action("update_label")
                    .details(Map.of("text", logMessage, "labelId", labelId))
                    .build());
        }

        // --- GỬI WEBSOCKET ---
        broadcastLabelUpdate(existingLabel, WebSocketUpdateType.LABEL_UPDATED);
        return labelMapper.toLabelResponse(existingLabel);
    }

    // --- 4. DELETE ---
    public void deleteLabel(String labelId, String deletedByUserId) {
        Label existingLabel = labelRepository.findById(labelId)
                .orElseThrow(() -> new AppException(ErrorCode.LABEL_NOT_FOUND));

        labelRepository.deleteById(labelId);

        // TODO: (Phức tạp) Sau khi xóa nhãn, cần đảm bảo gỡ nhãn này khỏi tất cả các Card đang sử dụng nó.
        // Bạn sẽ cần inject CardRepository, tìm tất cả card có labelId này và gỡ nó ra.
        // cardRepository.removeLabelFromAllCards(labelId);

        // --- GHI LOG ---
        User deleter = userRepository.findById(deletedByUserId).orElse(null);
        if (deleter != null) {
            String logMessage = String.format("%s đã xóa nhãn '%s'",
                    deleter.getDisplayName(), existingLabel.getName());
            activityService.createActivity(ActivityCreateRequest.builder()
                    .boardId(existingLabel.getBoardId())
                    .userId(deletedByUserId)
                    .action("delete_label")
                    .details(Map.of("text", logMessage, "labelName", existingLabel.getName()))
                    .build());
        }
        broadcastLabelUpdate(existingLabel, WebSocketUpdateType.LABEL_DELETED);
    }
    private void broadcastLabelUpdate(Label label, WebSocketUpdateType type) {
        LabelResponse response = labelMapper.toLabelResponse(label);
        WebSocketUpdateResponse wsResponse = WebSocketUpdateResponse.builder()
                .type(type)
                .payload(response)
                .build();
        messagingTemplate.convertAndSend("/topic/board/" + label.getBoardId(), wsResponse);
    }
}