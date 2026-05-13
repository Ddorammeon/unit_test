package com.do_an.userservice.dto.doctor;

import lombok.Data;

import java.util.UUID;

@Data
public class DoctorResponse {
    private UUID userId;
    private String specializationCode;
    private String workingHospital;
    private String licenseNumber;
    private Integer consultationFeeAmount;
}