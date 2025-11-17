package com.example.trello.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BoardCreateRequest {
    @NotBlank(message = "TITLE_REQUIRED")
    @Size(max = 100, message = "TITLE_TOO_LONG")
    String title;
    String description;
    @NotBlank(message = "OWNER_ID_REQUIRED")
    String ownerId; // ID của người tạo
    @NotBlank(message = "VISIBILITY_REQUIRED")
    @Pattern(regexp = "PRIVATE|TEAM|PUBLIC", message = "INVALID_VISIBILITY")
    String visibility; // PRIVATE, TEAM, PUBLIC
}