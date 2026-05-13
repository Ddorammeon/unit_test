package com.do_an.userservice.dto.doctor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorCreateRequest {
    private UUID userId;
    private String specializationCode;
    private String workingHospital;
    private String licenseNumber;
    private Integer consultationFeeAmount;
}