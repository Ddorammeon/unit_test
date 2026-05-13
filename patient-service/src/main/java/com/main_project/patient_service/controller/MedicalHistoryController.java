package com.main_project.patient_service.controller;

import com.main_project.patient_service.dto.ConditionRequestDTO;
import com.main_project.patient_service.dto.ConditionResponseDTO;
import com.main_project.patient_service.dto.MedicalHistoryRequestDTO;
import com.main_project.patient_service.dto.MedicalHistoryResponseDTO;
import com.main_project.patient_service.service.IMedicalHistoryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patient-service/medical-histories")
@RequiredArgsConstructor
public class MedicalHistoryController {

    private final IMedicalHistoryService medicalHistoryService;

    @GetMapping
    public ResponseEntity<List<MedicalHistoryResponseDTO>> getAllMedicalHistories() {
        return ResponseEntity.ok(medicalHistoryService.getAllMedicalHistories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalHistoryResponseDTO> getMedicalHistory(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(medicalHistoryService.getMedicalHistoryById(id));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalHistoryResponseDTO>> getByPatient(@PathVariable UUID patientId) {
        return ResponseEntity.ok(medicalHistoryService.getMedicalHistoriesByPatient(patientId));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<MedicalHistoryResponseDTO>> getByAppointment(@PathVariable UUID appointmentId) {
        return ResponseEntity.ok(medicalHistoryService.getMedicalHistoriesByAppointment(appointmentId));
    }

    @PostMapping
    public ResponseEntity<MedicalHistoryResponseDTO> createMedicalHistory(
            @Valid @RequestBody MedicalHistoryRequestDTO request) {
        try {
            MedicalHistoryResponseDTO created = medicalHistoryService.createMedicalHistory(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicalHistoryResponseDTO> updateMedicalHistory(
            @PathVariable UUID id,
            @Valid @RequestBody MedicalHistoryRequestDTO request) {
        try {
            MedicalHistoryResponseDTO updated = medicalHistoryService.updateMedicalHistory(id, request);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicalHistory(@PathVariable UUID id) {
        try {
            medicalHistoryService.deleteMedicalHistory(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{medicalHistoryId}/conditions")
    public ResponseEntity<ConditionResponseDTO> addConditionToMedicalHistory(
            @PathVariable UUID medicalHistoryId,
            @Valid @RequestBody ConditionRequestDTO conditionRequest) {
        try {
            ConditionResponseDTO created = medicalHistoryService.addConditionToMedicalHistory(medicalHistoryId, conditionRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/byAppointmentId")
    public ResponseEntity<MedicalHistoryResponseDTO> updateMedicalHistoryByAppointmentId(
            @Valid @RequestBody MedicalHistoryRequestDTO request) {
        try {
            MedicalHistoryResponseDTO updated = medicalHistoryService.updateMedicalHistoryByAppointmentId(request);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
