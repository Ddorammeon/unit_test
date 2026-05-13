package com.main_project.appointment_service.dto;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class HoldSlotRequestDTO {
    private UUID doctorId;
    private UUID patientId;
    private ZonedDateTime appointmentStartTime;
    private ZonedDateTime appointmentEndTime;
    private List<UUID> medicalServiceIds; // dùng để FE lưu lại, backend không bắt buộc dùng ở đây
}