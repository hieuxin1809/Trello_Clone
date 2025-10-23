package com.example.trello.service;

import com.example.trello.dto.request.LabelCreateRequest;
import com.example.trello.dto.request.LabelUpdateRequest;
import com.example.trello.dto.response.LabelResponse;
import com.example.trello.exception.AppException;
import com.example.trello.exception.ErrorCode;
import com.example.trello.mapper.LabelMapper;
import com.example.trello.model.Label;
import com.example.trello.repository.LabelRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LabelService {
    LabelRepository labelRepository;
    LabelMapper labelMapper;

    // --- 1. CREATE ---
    public LabelResponse createLabel(LabelCreateRequest request) {
        // TODO: Kiểm tra xem tên nhãn đã tồn tại trong Board chưa
        Label label = labelMapper.toLabel(request);
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
    public LabelResponse updateLabel(String labelId, LabelUpdateRequest request) {
        Label existingLabel = labelRepository.findById(labelId)
                .orElseThrow(() -> new AppException(ErrorCode.LABEL_NOT_FOUND));

        labelMapper.updateLabel(existingLabel, request);

        existingLabel = labelRepository.save(existingLabel);
        return labelMapper.toLabelResponse(existingLabel);
    }

    // --- 4. DELETE ---
    public void deleteLabel(String labelId) {
        labelRepository.deleteById(labelId);
        // TODO: Sau khi xóa nhãn, cần đảm bảo gỡ nhãn này khỏi tất cả các Card đang sử dụng nó.
    }
}