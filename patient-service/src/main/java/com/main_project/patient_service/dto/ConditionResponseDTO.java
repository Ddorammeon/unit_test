package com.main_project.patient_service.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ConditionResponseDTO {
    private UUID id;
    private UUID medicalHistoryId;
    private Integer toothNumber;
    private String name;
    private String status;
    private String treatment;
    private String surface;
}