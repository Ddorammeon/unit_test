package com.main_project.doctor_service.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class WorkScheduleResponseDTO {
    private UUID id;
    private LocalDate workDate;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
}
