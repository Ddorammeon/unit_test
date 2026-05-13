package com.main_project.patient_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UnderlyingDiseaseDTO - Data Transfer Object for UnderlyingDisease
 *
 * Represents a patient's medical condition
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnderlyingDiseaseDTO {

    /**
     * Disease name (e.g., "Diabetes Type 2", "Hypertension")
     */
    private String name;

    /**
     * Current status: ACTIVE, CONTROLLED, RESOLVED
     */
    private String status;

    /**
     * Severity level: MILD, MODERATE, SEVERE
     */
    private String severity;

    /**
     * Whether clinically verified
     */
    private Boolean isVerified;

    /**
     * Additional notes about the condition
     */
    private String note;
}
