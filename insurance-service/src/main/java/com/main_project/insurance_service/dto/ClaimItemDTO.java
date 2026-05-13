package com.main_project.insurance_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimItemDTO {
    private UUID id;
    private Integer quantity;
    private Integer unitPrice;
    private Integer totalAmount;
    private Float insurancePayRatio;
    private Integer insurancePayAmount;
    private Integer patientPayAmount;
    private UUID insuranceClaimId;
    private UUID bhytCatalogueId;
    private BhytCatalogueDTO bhytCatalogue;
}

