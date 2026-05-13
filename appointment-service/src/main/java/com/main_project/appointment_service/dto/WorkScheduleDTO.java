package com.main_project.appointment_service.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class WorkScheduleDTO {
    private UUID id;
    private LocalDate workDate;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
}
