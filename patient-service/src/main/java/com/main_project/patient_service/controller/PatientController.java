package com.main_project.patient_service.controller;

import com.main_project.patient_service.dto.PatientRequestDTO;
import com.main_project.patient_service.dto.PatientResponseDTO;
import com.main_project.patient_service.service.IPatientService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * PatientController - REST API for Patient Management
 *
 * Provides CRUD operations for Patient aggregate.
 * All child entities (PatientAllergy, UnderlyingDisease, ToothIssue)
 * are managed through Patient operations.
 *
 * Base URL: /patient-service/patients
 */
@RestController
@RequestMapping("/patient-service/patients")
@RequiredArgsConstructor
public class PatientController {

    private final IPatientService patientService;

    /**
     * Create a new patient with all child entities.
     *
     * POST /patient-service/patients
     *
     * Request body should include:
     * - Basic patient info (id, name, dob, gender, phone, medicalHistoryNote)
     * - List of patient allergies (with allergyId reference to master data)
     * - List of underlying diseases
     * - List of tooth issues
     *
     * @param request patient data with all child entities
     * @return 201 CREATED with patient details
     * @return 404 NOT FOUND if referenced allergy doesn't exist
     */
    @PostMapping
    public ResponseEntity<PatientResponseDTO> createPatient(
            @Valid @RequestBody PatientRequestDTO request) {
        try {
            PatientResponseDTO created = patientService.createPatient(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (EntityNotFoundException ex) {
            // Allergy master data not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Update an existing patient.
     * Smart list synchronization handles child entity updates.
     *
     * PUT /patient-service/patients/{id}
     *
     * - Updates basic patient information
     * - Uses clear + add pattern for child lists (triggers orphanRemoval)
     * - Old child entities not in the request will be deleted automatically
     *
     * @param id patient ID
     * @param request updated patient data
     * @return 200 OK with updated patient details
     * @return 404 NOT FOUND if patient or referenced allergy doesn't exist
     */
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> updatePatient(
            @PathVariable UUID id,
            @Valid @RequestBody PatientRequestDTO request) {
        try {
            PatientResponseDTO updated = patientService.updatePatient(id, request);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Get patient by ID with all child entities.
     *
     * GET /patient-service/patients/{id}
     *
     * Returns full patient profile including:
     * - Basic information
     * - All patient allergies (with allergy details)
     * - All underlying diseases
     * - All tooth issues
     *
     * @param id patient ID
     * @return 200 OK with patient details
     * @return 404 NOT FOUND if patient doesn't exist
     */
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> getPatient(@PathVariable UUID id) {
        try {
            PatientResponseDTO patient = patientService.getPatientById(id);
            return ResponseEntity.ok(patient);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Get all patients.
     *
     * GET /patient-service/patients
     *
     * @return 200 OK with list of all patients
     */
    @GetMapping
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients() {
        List<PatientResponseDTO> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    /**
     * Delete a patient.
     * Cascade delete will remove all child entities automatically.
     *
     * DELETE /patient-service/patients/{id}
     *
     * Automatically deletes:
     * - All patient allergies
     * - All underlying diseases
     * - All tooth issues
     *
     * @param id patient ID
     * @return 204 NO CONTENT on success
     * @return 404 NOT FOUND if patient doesn't exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        try {
            patientService.deletePatient(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

