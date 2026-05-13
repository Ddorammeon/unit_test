package com.main_project.insurance_service.repository;

import com.main_project.insurance_service.entity.InsurancePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InsurancePolicyRepository extends JpaRepository<InsurancePolicy, UUID> {

    Optional<InsurancePolicy> findByPolicyNumber(String policyNumber);

    List<InsurancePolicy> findByPolicyType(String policyType);

    List<InsurancePolicy> findByStatus(String status);

    @Query("SELECT ip FROM InsurancePolicy ip WHERE ip.coverageAmount >= :minAmount")
    List<InsurancePolicy> findByCoverageAmountGreaterThanEqual(@Param("minAmount") Integer minAmount);

    @Query("SELECT ip FROM InsurancePolicy ip WHERE ip.policyType = :policyType AND ip.status = :status")
    List<InsurancePolicy> findByPolicyTypeAndStatus(@Param("policyType") String policyType, @Param("status") String status);

    boolean existsByPolicyNumber(String policyNumber);
}





