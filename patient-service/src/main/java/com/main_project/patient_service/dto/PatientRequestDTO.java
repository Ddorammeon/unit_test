package com.main_project.patient_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientRequestDTO {

    @NotNull(message = "Patient ID (user_id) is required")
    private UUID userId;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;

    @Pattern(regexp = "MALE|FEMALE|OTHER", message = "Gender must be MALE, FEMALE, or OTHER")
    private String gender;

    private String address;

    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Invalid phone number format")
    private String contactPhone;

    @Pattern(regexp = "A_POSITIVE|A_NEGATIVE|B_POSITIVE|B_NEGATIVE|AB_POSITIVE|AB_NEGATIVE|O_POSITIVE|O_NEGATIVE|UNKNOWN",
            message = "Invalid blood type")
    private String bloodType;

    private String insuranceNumber;

    @Builder.Default
    private List<PatientAllergyDTO> patientAllergies = new ArrayList<>();

    @Builder.Default
    private List<UnderlyingDiseaseDTO> underlyingDiseases = new ArrayList<>();

    @Builder.Default
    private List<ToothIssueDTO> toothIssues = new ArrayList<>();
}
