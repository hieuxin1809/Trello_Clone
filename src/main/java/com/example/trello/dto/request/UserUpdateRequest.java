package com.example.trello.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    @Size(min = 6, message = "PASSWORD_TOO_SHORT")
    String password;
    String displayName;
    String avatar;
    @Email(message = "INVALID_EMAIL_FORMAT")
    String secondaryEmail;
    String role;
}
