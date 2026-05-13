package com.main_project.doctor_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data
public class WorkScheduleRequestDTO {

    @NotNull
    private LocalDate workDate;

    @NotNull
    private ZonedDateTime startTime;

    @NotNull
    private ZonedDateTime endTime;
}
