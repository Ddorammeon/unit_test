package com.main_project.insurance_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceClaimDTO {
    private UUID id;
    private String status;
    private Integer patientPayAmount;
    private Integer totalClaimAmount;
    private Integer totalInsurancePay;
    private LocalDateTime claimDate;
    private LocalDateTime approvalDate;
    private String notes;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private UUID patientInsuranceId;
    private PatientInsuranceDTO patientInsurance;
}





