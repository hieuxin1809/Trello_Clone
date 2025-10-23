package com.example.trello.dto.request;

import com.example.trello.model.Board;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BoardMemberRequest {
    String userId; // ID của thành viên được thêm
    String role;   // Vai trò của thành viên (OWNER, MANAGER, MEMBER, GUEST)
    String addedBy; // ID của người thực hiện thao tác thêm
}