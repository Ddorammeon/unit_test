package com.do_an.userservice.dto.labtechnician;

import lombok.Data;

import java.util.UUID;

@Data
public class LabTechnicianResponse {
    private UUID userId;
    private String licenseNumber;
}