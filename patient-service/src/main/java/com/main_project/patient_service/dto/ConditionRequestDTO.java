package com.main_project.patient_service.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class ConditionRequestDTO {
    private UUID id;
    
    private Integer toothNumber;

    @Size(max = 255)
    private String name;

    @Size(max = 50)
    private String status;

    @Size(max = 255)
    private String treatment;

    @Size(max = 255)
    private String surface;
}