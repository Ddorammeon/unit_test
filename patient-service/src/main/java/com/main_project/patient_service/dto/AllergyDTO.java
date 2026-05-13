package com.main_project.patient_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * AllergyDTO - Data Transfer Object for Allergy Master Data
 *
 * Used for CRUD operations on the allergy catalog
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllergyDTO {

    /**
     * Allergy ID (auto-generated for creation)
     */
    private UUID id;

    /**
     * Allergy name (required)
     */
    @NotBlank(message = "Allergy name is required")
    private String name;

    /**
     * Allergy type (e.g., "FOOD", "DRUG", "ENVIRONMENTAL")
     */
    private String type;

    /**
     * Description of the allergy
     */
    private String description;
}
