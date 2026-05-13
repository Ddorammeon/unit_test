package com.main_project.doctor_service.controller;

import com.main_project.doctor_service.dto.DoctorDegreeResponseDTO;
import com.main_project.doctor_service.service.IDoctorDegreeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for read-only operations on DoctorDegree.
 * Since DoctorDegree has a composition relationship with Doctor,
 * all write operations (create, update, delete) should be performed through DoctorController.
 */
@RestController
@RequestMapping("/doctor-service/doctor-degrees")
@RequiredArgsConstructor
public class DoctorDegreeController {

    private final IDoctorDegreeService doctorDegreeService;

    @GetMapping
    public ResponseEntity<List<DoctorDegreeResponseDTO>> getAllDoctorDegrees() {
        return ResponseEntity.ok(doctorDegreeService.getAllDoctorDegrees());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorDegreeResponseDTO> getDoctorDegree(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(doctorDegreeService.getDoctorDegreeById(id));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<DoctorDegreeResponseDTO>> getByDoctor(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(doctorDegreeService.getDegreesByDoctorId(doctorId));
    }
}
