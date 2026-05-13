package com.main_project.patient_service.service;

import com.main_project.patient_service.dto.AllergyDTO;

import java.util.List;
import java.util.UUID;

/**
 * IAllergyService - Service Interface for Allergy Master Data
 *
 * Manages the allergy catalog (master data) used for lookups.
 */
public interface IAllergyService {

    /**
     * Creates a new allergy in the catalog.
     *
     * @param dto allergy data
     * @return created allergy
     */
    AllergyDTO createAllergy(AllergyDTO dto);

    /**
     * Updates an existing allergy.
     *
     * @param id allergy ID
     * @param dto updated allergy data
     * @return updated allergy
     */
    AllergyDTO updateAllergy(UUID id, AllergyDTO dto);

    /**
     * Retrieves an allergy by ID.
     *
     * @param id allergy ID
     * @return allergy data
     */
    AllergyDTO getAllergyById(UUID id);

    /**
     * Retrieves all allergies from the catalog.
     *
     * @return list of all allergies
     */
    List<AllergyDTO> getAllAllergies();

    /**
     * Deletes an allergy from the catalog.
     *
     * @param id allergy ID
     */
    void deleteAllergy(UUID id);
}
