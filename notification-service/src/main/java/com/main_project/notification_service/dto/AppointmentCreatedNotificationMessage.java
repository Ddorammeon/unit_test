package com.main_project.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCreatedNotificationMessage {
    private String type;
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String appointmentStartTime;
    private String appointmentEndTime;
    private String message;
    private Long timestamp;
}