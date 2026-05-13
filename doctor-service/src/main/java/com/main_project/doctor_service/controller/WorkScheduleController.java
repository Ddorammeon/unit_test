package com.main_project.doctor_service.controller;

import com.main_project.doctor_service.dto.WorkScheduleRequestDTO;
import com.main_project.doctor_service.dto.WorkScheduleResponseDTO;
import com.main_project.doctor_service.service.IWorkScheduleService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/doctor-service/work-schedules")
@RequiredArgsConstructor
public class WorkScheduleController {

    private final IWorkScheduleService workScheduleService;

    @GetMapping
    public ResponseEntity<List<WorkScheduleResponseDTO>> getAllWorkSchedules() {
        return ResponseEntity.ok(workScheduleService.getAllWorkSchedules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkScheduleResponseDTO> getWorkSchedule(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(workScheduleService.getWorkScheduleById(id));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<WorkScheduleResponseDTO> createWorkSchedule(@Valid @RequestBody WorkScheduleRequestDTO request) {
        WorkScheduleResponseDTO created = workScheduleService.createWorkSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkScheduleResponseDTO> updateWorkSchedule(
            @PathVariable UUID id,
            @Valid @RequestBody WorkScheduleRequestDTO request) {
        try {
            WorkScheduleResponseDTO updated = workScheduleService.updateWorkSchedule(id, request);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkSchedule(@PathVariable UUID id) {
        try {
            workScheduleService.deleteWorkSchedule(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
