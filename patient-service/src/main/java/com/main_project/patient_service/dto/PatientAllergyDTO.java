package com.main_project.patient_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * PatientAllergyDTO - Data Transfer Object for PatientAllergy
 *
 * For input: allergyId is required to reference master data
 * For output: allergyCode and allergyName are populated for display
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientAllergyDTO {

    /**
     * Reference to Allergy master data (required for input)
     */
    private UUID allergyId;

    /**
     * Allergy code (populated for output)
     */
    private String allergyCode;

    /**
     * Allergy name (populated for output)
     */
    private String allergyName;

    /**
     * Severity level: MILD, MODERATE, SEVERE
     */
    private String severity;

    /**
     * Patient's reaction to the allergy
     */
    private String reaction;

    /**
     * Additional notes
     */
    private String note;
}
