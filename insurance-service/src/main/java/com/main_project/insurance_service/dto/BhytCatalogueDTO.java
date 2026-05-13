package com.main_project.insurance_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BhytCatalogueDTO {
    private UUID id;
    private String serviceCode;
    private String serviceName;
    private String serviceType;
    private Boolean isCovered;
    private Integer maxCoverageAmount;
}
