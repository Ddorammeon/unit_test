package com.main_project.doctor_service.dto;

import com.main_project.doctor_service.enums.SpecializationCodeEnum;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class DoctorResponseDTO {
    private UUID userId;
    private SpecializationCodeEnum specializationCode;
    private String workingHospital;
    private String licenseNumber;
    private Integer consultationFeeAmount;
    private List<DoctorDegreeResponseDTO> degrees = new ArrayList<>();
}
