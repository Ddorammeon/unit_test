package com.main_project.appointment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartAppointmentResponse {
    private UUID medicalHistoryId;
    private UUID invoiceId;
    private String status;
}
