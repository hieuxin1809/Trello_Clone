package com.example.trello.mapper;

import com.example.trello.dto.request.UserCreateRequest;
import com.example.trello.dto.request.UserUpdateRequest;
import com.example.trello.dto.response.UserResponse;
import com.example.trello.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreateRequest user);
    UserResponse toUserResponse(User user);
    @Mapping(target = "updatedAt", expression = "java(java.time.Instant.now())")
    void updateUser(@MappingTarget User user, UserUpdateRequest userUpdateRequest);
}
