package com.main_project.insurance_service.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsurancePolicyDTO {
    private UUID id;
    private String policyNumber;
    private String policyType;
    private Integer coverageAmount;
    private Integer deductible;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}





