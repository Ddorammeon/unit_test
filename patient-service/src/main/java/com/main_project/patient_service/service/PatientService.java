package com.main_project.patient_service.service;

import com.main_project.patient_service.dto.*;
import com.main_project.patient_service.entity.*;
import com.main_project.patient_service.enums.BloodType;
import com.main_project.patient_service.enums.Gender;
import com.main_project.patient_service.repository.AllergyRepository;
import com.main_project.patient_service.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * PatientService - Application Service for Patient Aggregate
 *
 * Implements DDD principles:
 * - Patient is the Aggregate Root
 * - All child operations through Patient entity methods
 * - Only PatientRepository and AllergyRepository used
 * - Smart list synchronization for updates (clear + add to trigger orphanRemoval)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PatientService implements IPatientService {

    private final PatientRepository patientRepository;
    private final AllergyRepository allergyRepository;

    /**
     * Creates a new patient with all child entities.
     * Uses aggregate methods and cascading.
     */
    @Override
    public PatientResponseDTO createPatient(PatientRequestDTO request) {
        // Create patient aggregate root
        Patient patient = Patient.builder()
                .userId(request.getUserId())
                .dob(request.getDob())
                .gender(Gender.fromString(request.getGender()))
                .address(request.getAddress())
                .contactPhone(request.getContactPhone())
                .bloodType(BloodType.fromString(request.getBloodType()))
                .insuranceNumber(request.getInsuranceNumber())
                .build();

        // Add patient allergies using aggregate method
        if (request.getPatientAllergies() != null) {
            for (PatientAllergyDTO allergyDTO : request.getPatientAllergies()) {
                // IMPORTANT: Fetch Allergy master data
                Allergy allergy = allergyRepository.findById(allergyDTO.getAllergyId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Allergy not found with id: " + allergyDTO.getAllergyId()));

                // Add using aggregate method
                patient.addPatientAllergy(
                        allergy,
                        allergyDTO.getSeverity(),
                        allergyDTO.getReaction(),
                        allergyDTO.getNote()
                );
            }
        }

        // Add underlying diseases using aggregate method
        if (request.getUnderlyingDiseases() != null) {
            for (UnderlyingDiseaseDTO diseaseDTO : request.getUnderlyingDiseases()) {
                patient.addUnderlyingDisease(
                        diseaseDTO.getName(),
                        diseaseDTO.getStatus(),
                        diseaseDTO.getSeverity(),
                        diseaseDTO.getIsVerified(),
                        diseaseDTO.getNote()
                );
            }
        }

        // Add tooth issues using aggregate method
        if (request.getToothIssues() != null) {
            for (ToothIssueDTO toothDTO : request.getToothIssues()) {
                patient.addToothIssue(
                        toothDTO.getToothNumber(),
                        toothDTO.getStatus(),
                        toothDTO.getDescription(),
                        toothDTO.getDiagnosedDate(),
                        toothDTO.getNote()
                );
            }
        }

        // Save patient (cascades to all children)
        Patient saved = patientRepository.save(patient);

        return mapToResponseDTO(saved);
    }

    /**
     * Updates an existing patient.
     * IMPORTANT: Uses smart list sync (clear + add) to trigger orphanRemoval.
     */
    @Override
    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO request) {
        Patient patient = patientRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + id));

        // Update basic info using aggregate method
        if (request.getDob() != null) {
            patient.setDob(request.getDob());
        }
        if (request.getGender() != null) {
            patient.setGender(Gender.fromString(request.getGender()));
        }
        if (request.getAddress() != null) {
            patient.setAddress(request.getAddress());
        }
        if (request.getContactPhone() != null) {
            patient.setContactPhone(request.getContactPhone());
        }
        if (request.getBloodType() != null) {
            patient.setBloodType(BloodType.fromString(request.getBloodType()));
        }
        if (request.getInsuranceNumber() != null) {
            patient.setInsuranceNumber(request.getInsuranceNumber());
        }

        // Smart List Sync for PatientAllergies
        syncPatientAllergies(patient, request.getPatientAllergies());

        // Smart List Sync for UnderlyingDiseases
        syncUnderlyingDiseases(patient, request.getUnderlyingDiseases());

        // Smart List Sync for ToothIssues
        syncToothIssues(patient, request.getToothIssues());

        // Save (orphanRemoval will delete removed items)
        Patient saved = patientRepository.save(patient);

        return mapToResponseDTO(saved);
    }

    /**
     * Smart sync for patient allergies.
     * Clears existing and adds new ones to trigger orphanRemoval.
     */
    private void syncPatientAllergies(Patient patient, List<PatientAllergyDTO> allergyDTOs) {
        // Clear existing (triggers orphanRemoval for deleted items)
        if (allergyDTOs == null) {
            return;
        }

        patient.clearPatientAllergies();

        // Add new/updated items using aggregate method
        for (PatientAllergyDTO dto : allergyDTOs) {
            Allergy allergy = allergyRepository.findById(dto.getAllergyId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Allergy not found with id: " + dto.getAllergyId()));

            patient.addPatientAllergy(
                    allergy,
                    dto.getSeverity(),
                    dto.getReaction(),
                    dto.getNote()
            );
        }
    }

    /**
     * Smart sync for underlying diseases.
     */
    private void syncUnderlyingDiseases(Patient patient, List<UnderlyingDiseaseDTO> diseaseDTOs) {
        if (diseaseDTOs == null) {
            return;
        }

        patient.clearUnderlyingDiseases();

        for (UnderlyingDiseaseDTO dto : diseaseDTOs) {
            patient.addUnderlyingDisease(
                    dto.getName(),
                    dto.getStatus(),
                    dto.getSeverity(),
                    dto.getIsVerified(),
                    dto.getNote()
            );
        }
    }

    /**
     * Smart sync for tooth issues.
     */
    private void syncToothIssues(Patient patient, List<ToothIssueDTO> toothDTOs) {
        if (toothDTOs == null) {
            return; // keep existing if not provided
        }

        patient.clearToothIssues();

        for (ToothIssueDTO dto : toothDTOs) {
            patient.addToothIssue(
                    dto.getToothNumber(),
                    dto.getStatus(),
                    dto.getDescription(),
                    dto.getDiagnosedDate(),
                    dto.getNote()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponseDTO getPatientById(UUID id) {
//        Patient patient = patientRepository.findByIdWithDetails(id)
//                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + id));
        Patient patient = patientRepository.findByUserId(id)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found with id: " + id));
        return mapToResponseDTO(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponseDTO> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePatient(UUID id) {
        if (!patientRepository.existsById(id)) {
            throw new EntityNotFoundException("Patient not found with id: " + id);
        }
        patientRepository.deleteById(id);
        // Cascade delete will remove all children
    }

    /**
     * Maps Patient entity to PatientResponseDTO.
     */
    private PatientResponseDTO mapToResponseDTO(Patient patient) {
        return PatientResponseDTO.builder()
                .userId(patient.getUserId())
                .dob(patient.getDob())
                .gender(patient.getGender() != null ? patient.getGender().name() : null)
                .address(patient.getAddress())
                .contactPhone(patient.getContactPhone())
                .bloodType(patient.getBloodType() != null ? patient.getBloodType().name() : null)
                .insuranceNumber(patient.getInsuranceNumber())
                .patientAllergies(mapPatientAllergiesToDTO(patient.getPatientAllergies()))
                .underlyingDiseases(mapUnderlyingDiseasesToDTO(patient.getUnderlyingDiseases()))
                .toothIssues(mapToothIssuesToDTO(patient.getToothIssues()))
                .build();
    }

    private List<PatientAllergyDTO> mapPatientAllergiesToDTO(Collection<PatientAllergy> allergies) {
        return allergies.stream()
                .map(pa -> PatientAllergyDTO.builder()
                        .allergyId(pa.getAllergy().getId())
                        .allergyName(pa.getAllergy().getName())
                        .severity(pa.getSeverity())
                        .reaction(pa.getReaction())
                        .note(pa.getNote())
                        .build())
                .collect(Collectors.toList());
    }

    private List<UnderlyingDiseaseDTO> mapUnderlyingDiseasesToDTO(Collection<UnderlyingDisease> diseases) {
        return diseases.stream()
                .map(d -> UnderlyingDiseaseDTO.builder()
                        .name(d.getName())
                        .status(d.getStatus())
                        .severity(d.getSeverity())
                        .isVerified(d.getIsVerified())
                        .note(d.getNote())
                        .build())
                .collect(Collectors.toList());
    }

    private List<ToothIssueDTO> mapToothIssuesToDTO(Collection<ToothIssue> toothIssues) {
        return toothIssues.stream()
                .map(t -> ToothIssueDTO.builder()
                        .toothNumber(t.getToothNumber())
                        .status(t.getStatus())
                        .description(t.getDescription())
                        .diagnosedDate(t.getDiagnosedDate())
                        .note(t.getNote())
                        .build())
                .collect(Collectors.toList());
    }
}
