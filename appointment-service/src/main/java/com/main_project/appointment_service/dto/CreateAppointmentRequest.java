package com.main_project.appointment_service.dto;

// Đây là file bạn tự tạo: CreateAppointmentRequest.java

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
public class CreateAppointmentRequest {

    private UUID patientId;
    private UUID doctorId;
    private ZonedDateTime startTime;
}
