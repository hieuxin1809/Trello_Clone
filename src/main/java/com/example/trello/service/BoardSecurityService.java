package com.example.trello.service;

import com.example.trello.exception.AppException;

import com.example.trello.exception.ErrorCode;

import com.example.trello.model.Board;

import com.example.trello.model.Card;
import com.example.trello.model.ListEntity;
import com.example.trello.model.User;

import com.example.trello.repository.BoardRepository;

import com.example.trello.repository.CardRepository;
import com.example.trello.repository.ListRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;



@Component("boardSecurityService") // Đặt tên cho bean để SpEL gọi
@RequiredArgsConstructor
public class BoardSecurityService {
    private final BoardRepository boardRepository;
    private final CardRepository cardRepository;
    private final ListRepository listRepository;
    /**
     * Kiểm tra xem user có phải là thành viên của board không
     */
    public boolean isMember(String boardId, User user) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));
        return board.getMembers().stream()
                .anyMatch(m -> m.getUserId().equals(user.getId()));
    }
    /**

     * Kiểm tra xem user có quyền từ MEMBER trở lên không

     */
    public boolean isAtLeastMember(String boardId, User user) {
        return hasPermission(boardId, user, Board.BoardMember.MemberRole.MEMBER);
    }
    /**
     * Kiểm tra xem user có quyền từ MANAGER trở lên không (MANAGER, OWNER)
     */
    public boolean isAtLeastManager(String boardId, User user) {
        return hasPermission(boardId, user, Board.BoardMember.MemberRole.MANAGER);
    }
    /**
     * Kiểm tra xem user có phải là OWNER không
     */
    public boolean isOwner(String boardId, User user) {
        return hasPermission(boardId, user, Board.BoardMember.MemberRole.OWNER);
    }
    public boolean isMemberOfCard(String cardId, User user) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.CARD_NOT_FOUND));

        // Gọi lại hàm isAtLeastMember để kiểm tra
        return hasPermission(card.getBoardId(), user, Board.BoardMember.MemberRole.MEMBER);
    }
    public boolean isManagerOfCard(String cardId, User user) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.CARD_NOT_FOUND));

        return hasPermission(card.getBoardId(), user, Board.BoardMember.MemberRole.MANAGER);
    }
    public boolean isMemberOfList(String listId, User user) {
        ListEntity list = listRepository.findById(listId)
                .orElseThrow(() -> new AppException(ErrorCode.LIST_NOT_FOUND));
        return hasPermission(list.getBoardId(), user, Board.BoardMember.MemberRole.MEMBER);
    }

    /**
     * HÀM MỚI: Kiểm tra user có phải là MANAGER của board chứa list này không
     */
    public boolean isManagerOfList(String listId, User user) {
        ListEntity list = listRepository.findById(listId)
                .orElseThrow(() -> new AppException(ErrorCode.LIST_NOT_FOUND));
        return hasPermission(list.getBoardId(), user, Board.BoardMember.MemberRole.MANAGER);
    }
    // Hàm private chung
    private boolean hasPermission(String boardId, User user, Board.BoardMember.MemberRole requiredRole) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new AppException(ErrorCode.BOARD_NOT_FOUND));
        Board.BoardMember member = board.getMembers().stream()
                .filter(m -> m.getUserId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_IN_BOARD)); // Không phải là thành viên
        if (member == null) {
            return false;
        }
        // So sánh: OWNER(0), MANAGER(1), MEMBER(2)
        // Nếu role của user (vd: MEMBER(2)) <= role yêu cầu (vd: MEMBER(2)) -> OK
        // Nếu role của user (vd: MANAGER(1)) <= role yêu cầu (vd: MEMBER(2)) -> OK
        // Nếu role của user (vd: MEMBER(2)) > role yêu cầu (vd: MANAGER(1)) -> Fail
        return member.getRole().ordinal() <= requiredRole.ordinal();

    }

}