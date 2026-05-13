package com.main_project.insurance_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsurancePolicyRequestDTO {
    
    @NotBlank(message = "Policy number is required")
    private String policyNumber;
    
    @NotBlank(message = "Policy type is required")
    private String policyType;
    
    @NotNull(message = "Coverage amount is required")
    @Positive(message = "Coverage amount must be positive")
    private Integer coverageAmount;
    
    @NotNull(message = "Deductible is required")
    @Positive(message = "Deductible must be positive")
    private Integer deductible;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    @NotBlank(message = "Status is required")
    private String status;
}





