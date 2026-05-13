package com.main_project.insurance_service.repository;

import com.main_project.insurance_service.entity.InsuranceClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, UUID> {

    List<InsuranceClaim> findByStatus(String status);

    List<InsuranceClaim> findByPatientInsuranceId(UUID patientInsuranceId);

    @Query("SELECT ic FROM InsuranceClaim ic WHERE ic.patientInsurance.patientId = :patientId")
    List<InsuranceClaim> findByPatientId(@Param("patientId") UUID patientId);

    @Query("SELECT ic FROM InsuranceClaim ic WHERE ic.claimDate BETWEEN :startDate AND :endDate")
    List<InsuranceClaim> findByClaimDateBetween(@Param("startDate") ZonedDateTime startDate,
                                                @Param("endDate") ZonedDateTime endDate);

    @Query("SELECT ic FROM InsuranceClaim ic WHERE ic.status = :status AND ic.totalClaimAmount >= :minAmount")
    List<InsuranceClaim> findByStatusAndTotalClaimAmountGreaterThanEqual(@Param("status") String status,
                                                                   @Param("minAmount") Integer minAmount);

    @Query("SELECT COUNT(ic) FROM InsuranceClaim ic WHERE ic.patientInsurance.id = :patientInsuranceId")
    long countByPatientInsuranceId(@Param("patientInsuranceId") UUID patientInsuranceId);

    @Query("SELECT SUM(ic.totalInsurancePay) FROM InsuranceClaim ic WHERE ic.patientInsurance.id = :patientInsuranceId AND ic.status = 'APPROVED'")
    Integer getTotalApprovedAmountByPatientInsurance(@Param("patientInsuranceId") UUID patientInsuranceId);
}





