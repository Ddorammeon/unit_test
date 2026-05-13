package com.main_project.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabTestRequestedNotificationMessage {
    private String type;
    private String labTestId;
    private String appointmentId;
    private String medicalHistoryId;
    private String doctorId;
    private String labTestTypeId;
    private String message;
    private Long timestamp;
}