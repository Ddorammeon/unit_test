package com.do_an.userservice.dto.labtechnician;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabTechnicianCreateRequest {
    private UUID userId;
    private String licenseNumber;
}