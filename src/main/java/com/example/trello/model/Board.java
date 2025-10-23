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
@Document(collection = "boards")
public class Board {
    @Id
    private String id;

    private String title;
    private String description;

    private Background background = new Background();

    @Indexed
    private String ownerId;

    private List<BoardMember> members;

    private Visibility visibility = Visibility.PRIVATE;
    private boolean isClosed = false;
    private boolean isArchived = false;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private BoardStats stats = new BoardStats();

    @Data
    public static class Background {
        private BackgroundType type = BackgroundType.COLOR;
        private String value = "#0079BF";

        public enum BackgroundType {
            COLOR, IMAGE
        }
    }

    @Data
    @Builder
    public static class BoardMember {
        private String userId;
        private MemberRole role;
        private Instant addedAt;
        private String addedBy;

        public enum MemberRole {
            OWNER, MANAGER, MEMBER, GUEST
        }
    }

    @Data
    public static class BoardStats {
        private int totalLists = 0;
        private int totalCards = 0;
        private int completedCards = 0;
    }

    public enum Visibility {
        PRIVATE, TEAM, PUBLIC
    }
}
