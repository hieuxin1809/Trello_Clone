package com.example.trello.service;

import com.example.trello.dto.request.ListCreateRequest;
import com.example.trello.dto.request.ListUpdateRequest;
import com.example.trello.dto.response.ListResponse;
import com.example.trello.exception.AppException;
import com.example.trello.exception.ErrorCode;
import com.example.trello.mapper.ListMapper;
import com.example.trello.model.ListEntity;
import com.example.trello.repository.ListRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ListService {
    ListRepository listRepository;
    ListMapper listMapper;

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
    public ListResponse updateList(String listId, ListUpdateRequest request) {
        ListEntity existingList = listRepository.findById(listId)
                .orElseThrow(() -> new AppException(ErrorCode.LIST_NOT_FOUND));

        listMapper.updateList(existingList, request);

        existingList = listRepository.save(existingList);
        return listMapper.toListResponse(existingList);
    }

    // --- 4. DELETE (Archive) ---
    public void archiveList(String listId) {
        ListEntity list = listRepository.findById(listId)
                .orElseThrow(() -> new AppException(ErrorCode.LIST_NOT_FOUND));

        list.setArchived(true);
        listRepository.save(list);
    }
}
