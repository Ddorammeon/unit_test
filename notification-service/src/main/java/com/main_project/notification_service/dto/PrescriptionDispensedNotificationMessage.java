package com.main_project.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDispensedNotificationMessage {
    private String type; // "PRESCRIPTION_DISPENSED"
    private String dispenseOrderId;
    private String prescriptionId;
    private String medicalHistoryId;
    private String appointmentId;
    private String pharmacistId;
    private String pharmacistName;
    private String message;
    private Long timestamp;
}

