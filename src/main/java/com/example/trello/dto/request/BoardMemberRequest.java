package com.example.trello.dto.request;

import com.example.trello.model.Board;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BoardMemberRequest {
    @NotBlank(message = "USER_ID_REQUIRED")
    String userId; // ID của thành viên được thêm
    @NotBlank(message = "ROLE_REQUIRED")
    @Pattern(regexp = "OWNER|MANAGER|MEMBER|GUEST", message = "INVALID_MEMBER_ROLE")
    String role;   // Vai trò của thành viên (OWNER, MANAGER, MEMBER, GUEST)
    @NotBlank(message = "ADDED_BY_REQUIRED")
    String addedBy; // ID của người thực hiện thao tác thêm
}