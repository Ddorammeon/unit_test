package com.main_project.insurance_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BhytCatalogueRequestDTO {
    private String serviceCode;
    private String serviceName;
    private String serviceType;
    private Boolean isCovered;
    private Integer maxCoverageAmount;
}
