package com.example.trello.enums;

public enum WebSocketUpdateType {
    BOARD_MEMBER_REMOVED,
    BOARD_MEMBER_ADDED,
    BOARD_UPDATED,
    // Card Actions
    CARD_CREATED,
    CARD_UPDATED,
    CARD_MOVED,
    CARD_ARCHIVED,
    CARD_ASSIGNED,
    CARD_UNASSIGNED,

    // List Actions
    LIST_CREATED,
    LIST_UPDATED,
    LIST_ARCHIVED,

    // Comment Actions
    COMMENT_CREATED,
    COMMENT_UPDATED,
    COMMENT_DELETED,

    // Label Actions
    LABEL_CREATED,
    LABEL_UPDATED,
    LABEL_DELETED,

    // Bạn có thể thêm các type khác sau
    NOTIFICATION_CREATED,
    NOTIFICATION_MARKED_AS_READ
}