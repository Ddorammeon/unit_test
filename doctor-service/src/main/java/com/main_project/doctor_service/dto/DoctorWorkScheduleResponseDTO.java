package com.main_project.doctor_service.dto;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class DoctorWorkScheduleResponseDTO {
    private UUID id;
    private String status;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private UUID workScheduleId;
    private UUID doctorId;
}
