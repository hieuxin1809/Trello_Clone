package com.example.trello.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreateRequest {
    String email;
    String password;
    String displayName;
    String avatar;
    String secondaryEmail;
    String role;
}
