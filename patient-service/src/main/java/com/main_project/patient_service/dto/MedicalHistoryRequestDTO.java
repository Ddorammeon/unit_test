package com.main_project.patient_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class MedicalHistoryRequestDTO {

    private UUID appointmentId;

    @Size(max = 255)
    private String symptoms;

    @NotNull(message = "patientId is required")
    private UUID patientId;

    private List<ConditionRequestDTO> conditions;
}
