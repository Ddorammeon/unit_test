package com.main_project.patient_service.repository;

import com.main_project.patient_service.entity.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * AllergyRepository - Repository for Master Data
 *
 * Provides CRUD operations for Allergy master data.
 * This is a reference data repository used for lookups.
 */
@Repository
public interface AllergyRepository extends JpaRepository<Allergy, UUID> {
    Optional<Allergy> findByName(String name);
}
