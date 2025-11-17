package com.example.trello.service;

import com.example.trello.dto.request.ActivityCreateRequest;
import com.example.trello.dto.request.ListCreateRequest;
import com.example.trello.dto.request.ListUpdateRequest;
import com.example.trello.dto.response.ListResponse;
import com.example.trello.dto.response.WebSocketUpdateResponse;
import com.example.trello.enums.WebSocketUpdateType;
import com.example.trello.exception.AppException;
import com.example.trello.exception.ErrorCode;
import com.example.trello.mapper.ListMapper;
import com.example.trello.model.ListEntity;
import com.example.trello.model.User;
import com.example.trello.repository.ListRepository;
import com.example.trello.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ListService {
    ListRepository listRepository;
    ListMapper listMapper;

    ActivityService activityService;
    UserRepository userRepository;
    SimpMessagingTemplate messagingTemplate;

    // --- 1. CREATE ---
    public ListResponse createList(ListCreateRequest request) {
        // 1. Tính toán vị trí (position) mới: Lấy max position hiện tại + 1
        int maxPosition = listRepository.findByBoardIdAndIsArchivedFalseOrderByPositionAsc(request.getBoardId())
                .stream()
                .mapToInt(ListEntity::getPosition)
                .max()
                .orElse(-1);

        ListEntity list = listMapper.toList(request);
        list.setPosition(maxPosition + 1); // Đặt vị trí mới

        list = listRepository.save(list);
        User creator = userRepository.findById(request.getCreatedBy())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (creator != null) {
            String logMessage = String.format("%s đã tạo danh sách '%s'", creator.getDisplayName(), list.getTitle());
            activityService.createActivity(ActivityCreateRequest.builder()
                    .boardId(request.getBoardId())
                    .listId(list.getId())
                    .userId(request.getCreatedBy())
                    .action("create_list")
                    .details(Map.of("text", logMessage))
                    .build());
        }
        broadcastListUpdate(list, WebSocketUpdateType.LIST_CREATED);
        return listMapper.toListResponse(list);
    }

    // --- 2. READ (Single) ---
    public ListResponse getListById(String listId) {
        ListEntity list = listRepository.findById(listId)
                .orElseThrow(() -> new AppException(ErrorCode.LIST_NOT_FOUND));
        return listMapper.toListResponse(list);
    }

    // --- 2. READ (All by Board) ---
    public List<ListResponse> getListsByBoard(String boardId) {
        List<ListEntity> lists = listRepository.findByBoardIdAndIsArchivedFalseOrderByPositionAsc(boardId);
        return lists.stream()
                .map(listMapper::toListResponse)
                .collect(Collectors.toList());
    }

    // --- 3. UPDATE ---
    public ListResponse updateList(String listId, ListUpdateRequest request,String updatedByUserId) {
        ListEntity existingList = listRepository.findById(listId)
                .orElseThrow(() -> new AppException(ErrorCode.LIST_NOT_FOUND));
        String oldTitle = existingList.getTitle();
        Integer oldPosition = existingList.getPosition();
        listMapper.updateList(existingList, request);

        existingList = listRepository.save(existingList);
        User updater = userRepository.findById(updatedByUserId).orElse(null);

        if (updater != null) {
            // (1) KIỂM TRA THAY ĐỔI TÊN
            if (request.getTitle() != null && !request.getTitle().equals(oldTitle)) {
                String logMessage = String.format("%s đã đổi tên danh sách từ '%s' thành '%s'",
                        updater.getDisplayName(), oldTitle, existingList.getTitle());
                activityService.createActivity(ActivityCreateRequest.builder()
                        .boardId(existingList.getBoardId())
                        .listId(listId)
                        .userId(updatedByUserId)
                        .action("update_list_title") // Dùng action cụ thể hơn
                        .details(Map.of("text", logMessage))
                        .build());
            }

            // (2) KIỂM TRA THAY ĐỔI VỊ TRÍ
            // Lưu ý: Logic di chuyển list thường phức tạp hơn (dùng float ordering)
            // nhưng ở đây ta giả định `position` là một số nguyên đơn giản
            if (request.getPosition() != null && !request.getPosition().equals(oldPosition)) {
                String logMessage = String.format("%s đã di chuyển danh sách '%s'",
                        updater.getDisplayName(), existingList.getTitle());
                activityService.createActivity(ActivityCreateRequest.builder()
                        .boardId(existingList.getBoardId())
                        .listId(listId)
                        .userId(updatedByUserId)
                        .action("move_list") // Dùng action cụ thể hơn
                        .details(Map.of("text", logMessage, "oldPosition", oldPosition, "newPosition", request.getPosition()))
                        .build());
            }
        }
        return listMapper.toListResponse(existingList);
    }

    // --- 4. DELETE (Archive) ---
    public void archiveList(String listId,String userId) {
        ListEntity list = listRepository.findById(listId)
                .orElseThrow(() -> new AppException(ErrorCode.LIST_NOT_FOUND));

        list.setArchived(true);
        listRepository.save(list);
        User archiver = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (archiver != null) {
            String logMessage = String.format("%s đã lưu trữ danh sách '%s'",
                    archiver.getDisplayName(), list.getTitle());
            activityService.createActivity(ActivityCreateRequest.builder()
                    .boardId(list.getBoardId())
                    .listId(listId)
                    .userId(userId)
                    .action("archive_list")
                    .details(Map.of("text", logMessage))
                    .build());
        }

        // --- GỬI WEBSOCKET ---
        broadcastListUpdate(list, WebSocketUpdateType.LIST_ARCHIVED);
    }
    private void broadcastListUpdate(ListEntity list, WebSocketUpdateType type) {
        ListResponse response = listMapper.toListResponse(list);
        WebSocketUpdateResponse wsResponse = WebSocketUpdateResponse.builder()
                .type(type)
                .payload(response)
                .build();
        messagingTemplate.convertAndSend("/topic/board/" + list.getBoardId(), wsResponse);
    }
}
