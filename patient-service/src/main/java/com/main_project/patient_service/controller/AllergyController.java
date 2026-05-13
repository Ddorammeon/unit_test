package com.main_project.patient_service.controller;

import com.main_project.patient_service.dto.AllergyDTO;
import com.main_project.patient_service.service.IAllergyService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * AllergyController - REST API for Allergy Master Data
 *
 * Manages the allergy catalog (master data) used for reference by PatientAllergy.
 *
 * Base URL: /patient-service/allergies
 */
@RestController
@RequestMapping("/patient-service/allergies")
@RequiredArgsConstructor
public class AllergyController {

    private final IAllergyService allergyService;

    /**
     * Create a new allergy in the catalog.
     *
     * POST /patient-service/allergies
     *
     * Creates a new allergy entry that can be referenced by patients.
     *
     * @param dto allergy data (name, type, description)
     * @return 201 CREATED with allergy details
     */
    @PostMapping
    public ResponseEntity<AllergyDTO> createAllergy(@Valid @RequestBody AllergyDTO dto) {
        AllergyDTO created = allergyService.createAllergy(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an existing allergy.
     *
     * PUT /patient-service/allergies/{id}
     *
     * Updates allergy master data.
     * Changes will be reflected in all patient allergies that reference it.
     *
     * @param id allergy ID
     * @param dto updated allergy data
     * @return 200 OK with updated allergy details
     * @return 404 NOT FOUND if allergy doesn't exist
     */
    @PutMapping("/{id}")
    public ResponseEntity<AllergyDTO> updateAllergy(
            @PathVariable UUID id,
            @Valid @RequestBody AllergyDTO dto) {
        try {
            AllergyDTO updated = allergyService.updateAllergy(id, dto);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Get allergy by ID.
     *
     * GET /patient-service/allergies/{id}
     *
     * @param id allergy ID
     * @return 200 OK with allergy details
     * @return 404 NOT FOUND if allergy doesn't exist
     */
    @GetMapping("/{id}")
    public ResponseEntity<AllergyDTO> getAllergy(@PathVariable UUID id) {
        try {
            AllergyDTO allergy = allergyService.getAllergyById(id);
            return ResponseEntity.ok(allergy);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Get all allergies from the catalog.
     *
     * GET /patient-service/allergies
     *
     * Returns the complete allergy catalog for lookup/selection purposes.
     *
     * @return 200 OK with list of all allergies
     */
    @GetMapping
    public ResponseEntity<List<AllergyDTO>> getAllAllergies() {
        List<AllergyDTO> allergies = allergyService.getAllAllergies();
        return ResponseEntity.ok(allergies);
    }

    /**
     * Delete an allergy from the catalog.
     *
     * DELETE /patient-service/allergies/{id}
     *
     * WARNING: This will fail if the allergy is referenced by any patient allergies.
     * The database constraint (ON DELETE RESTRICT) prevents orphaning references.
     *
     * @param id allergy ID
     * @return 204 NO CONTENT on success
     * @return 404 NOT FOUND if allergy doesn't exist
     * @return 409 CONFLICT if allergy is referenced by patients
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAllergy(@PathVariable UUID id) {
        try {
            allergyService.deleteAllergy(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception ex) {
            // Database constraint violation (allergy is referenced)
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
