package com.main_project.insurance_service.exceptions.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIED(1000, "Uncategoried error", HttpStatus.BAD_REQUEST),
    PATIENT_NOT_EXISTED(1002, "PATIENT not existed", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(1003, "Invalid credentials", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1004, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZATION(1005, "Unauthorization", HttpStatus.FORBIDDEN);

    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;
}
