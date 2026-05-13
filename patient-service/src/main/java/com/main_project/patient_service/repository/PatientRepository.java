package com.main_project.patient_service.repository;

import com.main_project.patient_service.entity.Patient;
import com.main_project.patient_service.enums.BloodType;
import com.main_project.patient_service.enums.Gender;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * PatientRepository - Repository for Aggregate Root
 *
 * IMPORTANT: This is the ONLY repository used for write operations
 * on the Patient aggregate (including PatientAllergy, UnderlyingDisease, ToothIssue).
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    @EntityGraph(attributePaths = {
        "patientAllergies",
        "underlyingDiseases",
        "toothIssues"
    })
    Optional<Patient> findByUserId(UUID userId);

    /**
     * Find patient with all relationships eagerly loaded.
     * Avoids N+1 query problem.
     */
    @Query("SELECT DISTINCT p FROM Patient p " +
           "LEFT JOIN FETCH p.patientAllergies pa " +
           "LEFT JOIN FETCH pa.allergy " +
           "LEFT JOIN FETCH p.underlyingDiseases " +
           "LEFT JOIN FETCH p.toothIssues " +
           "WHERE p.userId = :id")
    Optional<Patient> findByIdWithDetails(@Param("id") UUID id);

    /**
     * Find all patients by gender
     */
    List<Patient> findByGender(Gender gender);

    /**
     * Find all patients by blood type
     */
    List<Patient> findByBloodType(BloodType bloodType);

    /**
     * Find patients by contact phone
     */
    Optional<Patient> findByContactPhone(String contactPhone);

    /**
     * Find patients by insurance number
     */
    Optional<Patient> findByInsuranceNumber(String insuranceNumber);
}
