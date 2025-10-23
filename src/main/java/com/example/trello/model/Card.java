package com.example.trello.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "cards")
public class Card {
    @Id
    private String id;

    @Indexed
    private String listId;

    @Indexed
    private String boardId;

    private String title;
    private String description;
    @Indexed
    private double ordering;

    private List<CardLabel> labels ;
    private List<CardAssignee> assignees;

    private Instant dueDate;
    private Instant dueReminder;
    private boolean isCompleted = false;
    private Instant completedAt;

    private List<Checklist> checklists;
    private List<Attachment> attachments;

    private Cover cover;

    private boolean isArchived = false;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private String createdBy;

    @Data
    public static class CardLabel {
        private String id;
        private String name;
        private String color;
    }

    @Data
    @Builder
    public static class CardAssignee {
        private String userId;
        private Instant assignedAt;
        private String assignedBy;
    }

    @Data
    public static class Checklist {
        private String id;
        private String title;
        private List<ChecklistItem> items;
    }

    @Data
    public static class ChecklistItem {
        private String id;
        private String text;
        private boolean isCompleted = false;
        private Instant completedAt;
        private String completedBy;
    }

    @Data
    public static class Attachment {
        private String id;
        private String name;
        private String url;
        private long size;
        private String mimeType;
        private String uploadedBy;
        private Instant uploadedAt;
    }

    @Data
    public static class Cover {
        private CoverType type;
        private String value;

        public enum CoverType {
            ATTACHMENT, COLOR
        }
    }
}
