package com.example.trello.service;

import com.example.trello.exception.AppException;
import com.example.trello.exception.ErrorCode;
import com.example.trello.model.Comment;
import com.example.trello.model.User;
import com.example.trello.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("commentSecurityService") // Đặt tên cho bean để @PreAuthorize gọi
@RequiredArgsConstructor
public class CommentSecurityService {

    private final CommentRepository commentRepository;
    private final BoardSecurityService boardSecurityService; // (1) Dùng lại service cũ

    /**
     * Kiểm tra xem user có phải là TÁC GIẢ của comment không
     */
    public boolean isAuthor(String commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        return comment.getUserId().equals(user.getId());
    }

    /**
     * Kiểm tra xem user có phải là TÁC GIẢ
     * HOẶC là MANAGER/OWNER của board chứa comment đó không
     * (Để manager có thể xóa các comment không phù hợp)
     */
    public boolean isAuthorOrManager(String commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        if (comment.getUserId().equals(user.getId())) {
            return true;
        }
        //Nếu không, có phải là Manager của board không?
        return boardSecurityService.isAtLeastManager(comment.getBoardId(), user);
    }
}