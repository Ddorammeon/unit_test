package com.main_project.patient_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PatientResponseDTO - Response DTO for Patient
 *
 * Contains patient basic info and all child entity lists for display following the new schema
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponseDTO {

    /**
     * Patient ID (user_id from Identity Service)
     */
    private UUID userId;

    /**
     * Date of birth
     */
    private LocalDate dob;

    /**
     * Gender (MALE, FEMALE, OTHER)
     */
    private String gender;

    /**
     * Patient address
     */
    private String address;

    /**
     * Contact phone number
     */
    private String contactPhone;

    /**
     * Blood type (e.g., A+, O-, AB+, etc.)
     */
    private String bloodType;

    /**
     * Insurance number
     */
    private String insuranceNumber;

    /**
     * List of patient allergies with allergy details
     */
    @Builder.Default
    private List<PatientAllergyDTO> patientAllergies = new ArrayList<>();

    /**
     * List of underlying diseases
     */
    @Builder.Default
    private List<UnderlyingDiseaseDTO> underlyingDiseases = new ArrayList<>();

    /**
     * List of tooth issues
     */
    @Builder.Default
    private List<ToothIssueDTO> toothIssues = new ArrayList<>();
}
