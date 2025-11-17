package com.example.trello.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999,"Uncategorized exception" , HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1000,"Invalid Key" , HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(1001,"User not found" , HttpStatus.NOT_FOUND),
    EMAIL_EXIST(1002,"Email Exist" , HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1015,"UNAUTHORIZED" , HttpStatus.UNAUTHORIZED),
    BOARD_NOT_FOUND(1006,"board not found" , HttpStatus.NOT_FOUND),
    LIST_NOT_FOUND(1007,"list not found" , HttpStatus.NOT_FOUND),
    CARD_NOT_FOUND(1008,"card not found" , HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND(1009,"comment not found" , HttpStatus.NOT_FOUND),
    NOTIFICATION_NOT_FOUND(1010,"notification not found" , HttpStatus.NOT_FOUND),
    INVALID_OPERATION(1011,"invalid operation" , HttpStatus.BAD_REQUEST),
    LABEL_NOT_FOUND(1012,"label not found" , HttpStatus.NOT_FOUND),
    MEMBER_ALREADY_EXISTS(1013,"member already exists in board" , HttpStatus.BAD_REQUEST),
    MEMBER_NOT_FOUND_OR_IS_OWNER(1014,"member not found or is owner" , HttpStatus.BAD_REQUEST),
    USER_ALREADY_ASSIGNED(1016,"user already assigned to card" , HttpStatus.BAD_REQUEST),
    USER_NOT_ASSIGNED(1017,"user not assigned to card" , HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1018,"Unauthenticated" , HttpStatus.UNAUTHORIZED),
    INVALID_PASSWORD(1019,"old password invalid" , HttpStatus.BAD_REQUEST),
    USER_NOT_IN_BOARD(1020,"user not in board" , HttpStatus.BAD_REQUEST),
    TITLE_REQUIRED(1021,"title is required" , HttpStatus.BAD_REQUEST),
    OWNER_ID_REQUIRED(1022,"ownerId is required" , HttpStatus.BAD_REQUEST),
    VISIBILITY_REQUIRED(1023,"visibility is required" , HttpStatus.BAD_REQUEST),
    INVALID_VISIBILITY(1024,"invalid visibility" , HttpStatus.BAD_REQUEST),
    USER_ID_REQUIRED(1025,"userId is required" , HttpStatus.BAD_REQUEST),
    ROLE_REQUIRED(1026,"role is required" , HttpStatus.BAD_REQUEST),
    INVALID_MEMBER_ROLE(1027,"invalid member role" , HttpStatus.BAD_REQUEST),
    LIST_ID_REQUIRED(1028,"listId is required" , HttpStatus.BAD_REQUEST),
    BOARD_ID_REQUIRED(1029,"boardId is required" , HttpStatus.BAD_REQUEST),
    ADDED_BY_REQUIRED(1030,"addedBy is required" , HttpStatus.BAD_REQUEST),
    TITLE_TOO_LONG(1031,"title is too long" , HttpStatus.BAD_REQUEST),
    CREATOR_ID_REQUIRED(1032,"creatorId is required" , HttpStatus.BAD_REQUEST),
    DUE_DATE_MUST_BE_IN_FUTURE(1033,"dueDate must be in the future" , HttpStatus.BAD_REQUEST),
    CONTENT_REQUIRED(1034,"content is required" , HttpStatus.BAD_REQUEST),
    COMMENT_TOO_LONG(1035,"comment is too long" , HttpStatus.BAD_REQUEST),
    EMAIL_REQUIRED(1036,"email is required" , HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(1037,"password is required" , HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_FORMAT(1038,"invalid email format" , HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_SHORT(1039,"password is too short" , HttpStatus.BAD_REQUEST),
    ;


    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
