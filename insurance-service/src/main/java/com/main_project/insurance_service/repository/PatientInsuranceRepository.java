package com.main_project.insurance_service.repository;

import com.main_project.insurance_service.entity.PatientInsurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientInsuranceRepository extends JpaRepository<PatientInsurance, UUID> {

    Optional<PatientInsurance> findByPatientId(UUID patientId);

    List<PatientInsurance> findByStatus(String status);

    List<PatientInsurance> findByInsurancePolicyId(UUID insurancePolicyId);

    @Query("SELECT pi FROM PatientInsurance pi WHERE pi.expiryDate >= :currentDate AND pi.status = 'ACTIVE'")
    List<PatientInsurance> findActiveInsurances(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT pi FROM PatientInsurance pi WHERE pi.expiryDate < :currentDate")
    List<PatientInsurance> findExpiredInsurances(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT pi FROM PatientInsurance pi WHERE pi.patientId = :patientId AND pi.status = 'ACTIVE'")
    Optional<PatientInsurance> findActiveInsuranceByPatientId(@Param("patientId") UUID patientId);

    boolean existsByPatientId(UUID patientId);
}





