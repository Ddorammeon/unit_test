package com.main_project.insurance_service.controller;

import com.main_project.insurance_service.dto.PatientInsuranceDTO;
import com.main_project.insurance_service.dto.PatientInsuranceRequestDTO;
import com.main_project.insurance_service.service.IPatientInsuranceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/insurance-service/patient-insurances")
@RequiredArgsConstructor
@Tag(name = "Patient Insurance", description = "Patient Insurance Management APIs")
public class PatientInsuranceController {

    private final IPatientInsuranceService patientInsuranceService;

    @GetMapping
    @Operation(summary = "Get all patient insurances", description = "Retrieve all patient insurance records")
    public ResponseEntity<List<PatientInsuranceDTO>> getAllPatientInsurances() {
        List<PatientInsuranceDTO> patientInsurances = patientInsuranceService.getAllPatientInsurances();
        return ResponseEntity.ok(patientInsurances);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get patient insurance by ID", description = "Retrieve a specific patient insurance record by its ID")
    public ResponseEntity<PatientInsuranceDTO> getPatientInsuranceById(
            @Parameter(description = "Patient Insurance ID") @PathVariable UUID id) {
        Optional<PatientInsuranceDTO> patientInsurance = patientInsuranceService.getPatientInsuranceById(id);
        return patientInsurance.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get patient insurance by patient ID", description = "Retrieve patient insurance record by patient ID")
    public ResponseEntity<PatientInsuranceDTO> getPatientInsuranceByPatientId(
            @Parameter(description = "Patient ID") @PathVariable UUID patientId) {
        Optional<PatientInsuranceDTO> patientInsurance = patientInsuranceService.getPatientInsuranceByPatientId(patientId);
        return patientInsurance.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get patient insurances by status", description = "Retrieve patient insurance records by status")
    public ResponseEntity<List<PatientInsuranceDTO>> getPatientInsurancesByStatus(
            @Parameter(description = "Insurance Status") @PathVariable String status) {
        List<PatientInsuranceDTO> patientInsurances = patientInsuranceService.getPatientInsurancesByStatus(status);
        return ResponseEntity.ok(patientInsurances);
    }

    @GetMapping("/policy/{policyId}")
    @Operation(summary = "Get patient insurances by policy ID", description = "Retrieve patient insurance records by insurance policy ID")
    public ResponseEntity<List<PatientInsuranceDTO>> getPatientInsurancesByPolicyId(
            @Parameter(description = "Insurance Policy ID") @PathVariable UUID policyId) {
        List<PatientInsuranceDTO> patientInsurances = patientInsuranceService.getPatientInsurancesByPolicyId(policyId);
        return ResponseEntity.ok(patientInsurances);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active insurances", description = "Retrieve all active patient insurance records")
    public ResponseEntity<List<PatientInsuranceDTO>> getActiveInsurances() {
        List<PatientInsuranceDTO> activeInsurances = patientInsuranceService.getActiveInsurances();
        return ResponseEntity.ok(activeInsurances);
    }

    @GetMapping("/expired")
    @Operation(summary = "Get expired insurances", description = "Retrieve all expired patient insurance records")
    public ResponseEntity<List<PatientInsuranceDTO>> getExpiredInsurances() {
        List<PatientInsuranceDTO> expiredInsurances = patientInsuranceService.getExpiredInsurances();
        return ResponseEntity.ok(expiredInsurances);
    }

    //Dùng
    @GetMapping("/patient/{patientId}/active")
    @Operation(summary = "Get active insurance by patient ID", description = "Retrieve active insurance for a specific patient")
    public ResponseEntity<PatientInsuranceDTO> getActiveInsuranceByPatientId(
            @Parameter(description = "Patient ID") @PathVariable UUID patientId) {
        Optional<PatientInsuranceDTO> activeInsurance = patientInsuranceService.getActiveInsuranceByPatientId(patientId);
        return activeInsurance.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new patient insurance", description = "Create a new patient insurance record")
    public ResponseEntity<PatientInsuranceDTO> createPatientInsurance(
            @Valid @RequestBody PatientInsuranceRequestDTO requestDTO) {
        try {
            PatientInsuranceDTO createdPatientInsurance = patientInsuranceService.createPatientInsurance(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPatientInsurance);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update patient insurance", description = "Update an existing patient insurance record")
    public ResponseEntity<PatientInsuranceDTO> updatePatientInsurance(
            @Parameter(description = "Patient Insurance ID") @PathVariable UUID id,
            @Valid @RequestBody PatientInsuranceRequestDTO requestDTO) {
        try {
            PatientInsuranceDTO updatedPatientInsurance = patientInsuranceService.updatePatientInsurance(id, requestDTO);
            return ResponseEntity.ok(updatedPatientInsurance);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete patient insurance", description = "Delete a patient insurance record")
    public ResponseEntity<Void> deletePatientInsurance(
            @Parameter(description = "Patient Insurance ID") @PathVariable UUID id) {
        try {
            patientInsuranceService.deletePatientInsurance(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/exists/patient/{patientId}")
    @Operation(summary = "Check if patient has insurance", description = "Check if a patient already has insurance")
    public ResponseEntity<Boolean> existsByPatientId(
            @Parameter(description = "Patient ID") @PathVariable UUID patientId) {
        boolean exists = patientInsuranceService.existsByPatientId(patientId);
        return ResponseEntity.ok(exists);
    }
}
