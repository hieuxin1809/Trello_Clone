package com.example.trello.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "labels")
public class Label {
    @Id
    private String id;

    @Indexed
    private String boardId;

    private String name;
    private String color;

    @CreatedDate
    private Instant createdAt;

    private String createdBy;
}
