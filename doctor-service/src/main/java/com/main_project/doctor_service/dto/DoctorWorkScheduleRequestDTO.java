package com.main_project.doctor_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class DoctorWorkScheduleRequestDTO {

    @Size(max = 255)
    private String status;

    @NotNull(message = "workScheduleId is required")
    private UUID workScheduleId;

    @NotNull(message = "doctorId is required")
    private UUID doctorId;
}
