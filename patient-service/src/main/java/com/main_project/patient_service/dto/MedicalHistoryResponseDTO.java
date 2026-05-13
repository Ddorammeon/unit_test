package com.main_project.patient_service.dto;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class MedicalHistoryResponseDTO {
    private UUID id;
    private UUID appointmentId;
    private String symptoms;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private UUID patientId;
    private List<ConditionResponseDTO> conditions;
}
