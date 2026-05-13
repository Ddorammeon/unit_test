package com.main_project.patient_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * ToothIssueDTO - Data Transfer Object for ToothIssue
 *
 * Represents a specific dental issue for a patient
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToothIssueDTO {

    /**
     * Tooth number (1-32 for adults, dental notation)
     */
    private Integer toothNumber;

    /**
     * Issue status: ACTIVE, TREATED, PENDING
     */
    private String status;

    /**
     * Description of the dental issue (e.g., "Cavity", "Root canal needed")
     */
    private String description;

    /**
     * Date when the issue was diagnosed
     */
    private LocalDate diagnosedDate;

    /**
     * Additional notes about the tooth issue
     */
    private String note;
}
