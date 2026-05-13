package com.main_project.appointment_service.exceptions.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIED(1000, "Uncategoried error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1001, "User existed", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1002, "User not existed", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(1003, "Invalid credentials", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1004, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZATION(1005, "Unauthorization", HttpStatus.FORBIDDEN),
    PHONEANDPASSWONULL(1006, "phone và password không được để trống", HttpStatus.BAD_REQUEST),
    LIST_MEDICAL_SERVICE_EMPTY(1007, "At least one medical service must be provided", HttpStatus.BAD_REQUEST),
    INVALID_MEDICAL_SERVICE_ID(1008, "The provided medical service ID is invalid", HttpStatus.BAD_REQUEST),
    APPOINTMENT_NOT_EXISTED(1009, "Appointment not existed", HttpStatus.BAD_REQUEST),
    APPINTMENT_IS_NOT_CHECKIN_YET(1010, "Appointment is not checked-in yet", HttpStatus.BAD_REQUEST);
    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;
}
