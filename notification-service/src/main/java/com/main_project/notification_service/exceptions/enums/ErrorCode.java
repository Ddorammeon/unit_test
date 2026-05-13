package com.main_project.notification_service.exceptions.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIED(1000, "Uncategoried", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1001, "User not found", HttpStatus.NOT_FOUND),
    EMAIL_SENDING_FAILED(1002, "Sent email fail", HttpStatus.INTERNAL_SERVER_ERROR),
    TEMPLATE_NOT_FOUND(1003, "Template not found", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1004, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZATION(1005, "Unaithorization", HttpStatus.FORBIDDEN);

    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;
}