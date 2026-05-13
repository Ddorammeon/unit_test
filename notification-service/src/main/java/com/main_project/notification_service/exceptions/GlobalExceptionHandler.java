package com.main_project.notification_service.exceptions;

import com.main_project.notification_service.exceptions.enums.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ExceptionResponse> handlingException(Exception exception) {
        ErrorCode errorCode = ErrorCode.UNCATEGORIED;
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .code(errorCode.getCode())
                .message(exception.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(exceptionResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ExceptionResponse> handlingAppException(AppException appException) {
        ErrorCode errorCode = appException.getErrorCode();
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(exceptionResponse);
    }
}
