package com.main_project.appointment_service.dto;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class DoctorWorkScheduleDTO {
    private UUID id;
    private String status;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private UUID workScheduleId;
    private UUID doctorId;
}
