package com.example.trello.service;

import com.example.trello.dto.request.ActivityCreateRequest;
import com.example.trello.dto.response.ActivityResponse;
import com.example.trello.exception.AppException;
import com.example.trello.exception.ErrorCode; // Giả định có ErrorCode.BOARD_NOT_FOUND, CARD_NOT_FOUND
import com.example.trello.mapper.ActivityMapper;
import com.example.trello.model.Activity;
import com.example.trello.repository.ActivityRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivityService {
    ActivityRepository activityRepository;
    ActivityMapper activityMapper;

    // --- 1. CREATE (Được gọi bởi các Service khác, ví dụ CardService) ---
    public ActivityResponse createActivity(ActivityCreateRequest request) {
        Activity activity = activityMapper.toActivity(request);
        return activityMapper.toActivityResponse(activityRepository.save(activity));
    }

    // --- 2. READ (Lấy log theo Board) ---
    public List<ActivityResponse> getActivitiesByBoard(String boardId) {
        // Trello thường giới hạn số lượng Activity log trả về (ví dụ: 50)
        List<Activity> activities = activityRepository.findByBoardIdOrderByCreatedAtDesc(boardId);
        return activities.stream()
                .map(activityMapper::toActivityResponse)
                .collect(Collectors.toList());
    }

    // --- 2. READ (Lấy log theo Card) ---
    public List<ActivityResponse> getActivitiesByCard(String cardId) {
        List<Activity> activities = activityRepository.findByCardIdOrderByCreatedAtDesc(cardId);
        return activities.stream()
                .map(activityMapper::toActivityResponse)
                .collect(Collectors.toList());
    }
}
