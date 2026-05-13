package com.main_project.patient_service.service;

import com.main_project.patient_service.dto.PatientRequestDTO;
import com.main_project.patient_service.dto.PatientResponseDTO;

import java.util.List;
import java.util.UUID;

/**
 * IPatientService - Service Interface for Patient Aggregate
 *
 * Defines operations for managing patients and all their child entities
 * (PatientAllergy, UnderlyingDisease, ToothIssue).
 */
public interface IPatientService {

    /**
     * Creates a new patient with all child entities.
     * Uses aggregate methods and cascading.
     *
     * @param request patient data with child entities
     * @return created patient with all details
     */
    PatientResponseDTO createPatient(PatientRequestDTO request);

    /**
     * Updates an existing patient.
     * Uses smart list synchronization (clear + add) to trigger orphanRemoval.
     *
     * @param id patient ID
     * @param request updated patient data
     * @return updated patient with all details
     */
    PatientResponseDTO updatePatient(UUID id, PatientRequestDTO request);

    /**
     * Retrieves a patient by ID with all child entities.
     *
     * @param id patient ID
     * @return patient with all details
     */
    PatientResponseDTO getPatientById(UUID id);

    /**
     * Retrieves all patients.
     *
     * @return list of all patients
     */
    List<PatientResponseDTO> getAllPatients();

    /**
     * Deletes a patient.
     * Cascade delete will automatically remove all child entities.
     *
     * @param id patient ID
     */
    void deletePatient(UUID id);
}
