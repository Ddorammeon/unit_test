package com.main_project.doctor_service.controller;

import com.main_project.doctor_service.dto.DoctorRequestDTO;
import com.main_project.doctor_service.dto.DoctorResponseDTO;
import com.main_project.doctor_service.service.IDoctorService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/doctor-service/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final IDoctorService doctorService;

    @GetMapping
    public ResponseEntity<List<DoctorResponseDTO>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<DoctorResponseDTO> getDoctor(@PathVariable UUID userId) {
        try {
            return ResponseEntity.ok(doctorService.getDoctorById(userId));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<DoctorResponseDTO> createDoctor(@Valid @RequestBody DoctorRequestDTO request) {
        try {
            DoctorResponseDTO created = doctorService.createDoctor(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<DoctorResponseDTO> updateDoctor(
            @PathVariable UUID userId,
            @Valid @RequestBody DoctorRequestDTO request) {
        try {
            DoctorResponseDTO updated = doctorService.updateDoctor(userId, request);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable UUID userId) {
        try {
            doctorService.deleteDoctor(userId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
