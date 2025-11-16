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
    INVALID_PASSWORD(1019,"old password invalid" , HttpStatus.BAD_REQUEST)
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
