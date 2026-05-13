package com.main_project.insurance_service.repository;

import com.main_project.insurance_service.entity.ClaimItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClaimItemRepository extends JpaRepository<ClaimItem, UUID> {
    List<ClaimItem> findByInsuranceClaimId(UUID insuranceClaimId);
}

