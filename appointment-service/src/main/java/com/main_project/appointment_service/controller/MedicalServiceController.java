package com.main_project.appointment_service.controller;

import com.main_project.appointment_service.dto.MedicalServiceDTO;
import com.main_project.appointment_service.dto.MedicalServiceRequestDTO;
import com.main_project.appointment_service.service.IMedicalService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/appointment-service/medical-services")
@RequiredArgsConstructor
public class MedicalServiceController {

    private final IMedicalService service;

    @GetMapping
    @Operation(summary = "Get all medical services")
    public ResponseEntity<List<MedicalServiceDTO>> getAll() {
        return ResponseEntity.ok(service.getAllMedicalServices());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get medical service by ID")
    public ResponseEntity<MedicalServiceDTO> getById(@PathVariable UUID id) {
        Optional<MedicalServiceDTO> dto = service.getMedicalServiceById(id);
        return dto.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get medical services by type")
    public ResponseEntity<List<MedicalServiceDTO>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(service.getMedicalServicesByType(type));
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get medical services by exact name")
    public ResponseEntity<List<MedicalServiceDTO>> getByName(@PathVariable String name) {
        return ResponseEntity.ok(service.getMedicalServicesByName(name));
    }

    @GetMapping("/search")
    @Operation(summary = "Search medical services by keyword in name")
    public ResponseEntity<List<MedicalServiceDTO>> searchByName(@RequestParam String keyword) {
        return ResponseEntity.ok(service.searchMedicalServicesByName(keyword));
    }

    @GetMapping("/count/type/{type}")
    @Operation(summary = "Count medical services by type")
    public ResponseEntity<Long> countByType(@PathVariable String type) {
        return ResponseEntity.ok(service.countByServiceType(type));
    }

    @GetMapping("/count/name/{name}")
    @Operation(summary = "Count medical services by name")
    public ResponseEntity<Long> countByName(@PathVariable String name) {
        return ResponseEntity.ok(service.countByServiceName(name));
    }

    @PostMapping
    @Operation(summary = "Create a new medical service")
    public ResponseEntity<MedicalServiceDTO> create(@Valid @RequestBody MedicalServiceRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createMedicalService(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update existing medical service")
    public ResponseEntity<MedicalServiceDTO> update(@PathVariable UUID id,
                                                    @Valid @RequestBody MedicalServiceRequestDTO dto) {
        try {
            return ResponseEntity.ok(service.updateMedicalService(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete by ID")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        service.deleteMedicalService(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/type/{type}")
    @Operation(summary = "Delete all medical services by type")
    public ResponseEntity<Void> deleteByType(@PathVariable String type) {
        service.deleteByServiceType(type);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/name/{name}")
    @Operation(summary = "Delete all medical services by name")
    public ResponseEntity<Void> deleteByName(@PathVariable String name) {
        service.deleteByServiceName(name);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/deactivate/name/{name}")
    @Operation(summary = "Vô hiệu hóa dịch vụ theo tên")
    public ResponseEntity<?> deactivateByName(
            @PathVariable String name) {

        try {
            int result = service.deactivateMedicalServiceName(name);

            return ResponseEntity.ok(Map.of(
                    "message", "Đã vô hiệu hóa dịch vụ có tên: " + name,
                    "updatedRows", result
            ));
        } catch (Exception ex) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("error", ex.getMessage())
            );
        }
    }

    @PatchMapping("/deactivate/{id}")
    @Operation(summary = "Vô hiệu hóa dịch vụ theo ID")
    public ResponseEntity<?> deactivateById(
            @PathVariable UUID id) {

        try {
            int result = service.deactivateMedicalService(id);

            return ResponseEntity.ok(Map.of(
                    "message", "Đã vô hiệu hóa dịch vụ có ID: " + id,
                    "updatedRows", result
            ));
        } catch (Exception ex) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("error", ex.getMessage())
            );
        }
    }

    @PatchMapping("/deactivate/type/{type}")
    @Operation(summary = "Vô hiệu hóa tất cả dịch vụ theo loại")
    public ResponseEntity<?> deactivateByType(
            @PathVariable String type) {

        try {
            int result = service.deactivateMedicalServiceType(type);

            return ResponseEntity.ok(Map.of(
                    "message", "Đã vô hiệu hóa các dịch vụ thuộc loại: " + type,
                    "updatedRows", result
            ));
        } catch (Exception ex) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("error", ex.getMessage())
            );
        }
    }
}
