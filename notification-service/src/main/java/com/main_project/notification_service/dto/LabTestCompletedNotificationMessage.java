package com.main_project.notification_service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LabTestCompletedNotificationMessage {
    private String type;
    private String labTestId;
    private String appointmentId;
    private String message;

    public LabTestCompletedNotificationMessage(String type, String labTestId, String appointmentId, String message) {
        this.type = type;
        this.labTestId = labTestId;
        this.appointmentId = appointmentId;
        this.message = message;
    }
}
