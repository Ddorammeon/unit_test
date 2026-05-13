package com.main_project.insurance_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientInsuranceRequestDTO {
    
    @NotNull(message = "Patient ID is required")
    private UUID patientId;
    
    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;
    
    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;
    
    @NotNull(message = "Status is required")
    private String status;
    
    @NotNull(message = "Insurance policy ID is required")
    private UUID insurancePolicyId;
}





