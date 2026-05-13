package com.main_project.insurance_service.repository;

import com.main_project.insurance_service.entity.ClaimDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClaimDocumentRepository extends JpaRepository<ClaimDocument, UUID> {

    List<ClaimDocument> findByInsuranceClaimId(UUID insuranceClaimId);

    List<ClaimDocument> findByDocumentType(String documentType);

    List<ClaimDocument> findByStatus(String status);

    @Query("SELECT cd FROM ClaimDocument cd WHERE cd.insuranceClaim.id = :claimId AND cd.documentType = :documentType")
    List<ClaimDocument> findByClaimIdAndDocumentType(@Param("claimId") UUID claimId, 
                                                     @Param("documentType") String documentType);

    @Query("SELECT cd FROM ClaimDocument cd WHERE cd.insuranceClaim.patientInsurance.patientId = :patientId")
    List<ClaimDocument> findByPatientId(@Param("patientId") UUID patientId);

    @Query("SELECT COUNT(cd) FROM ClaimDocument cd WHERE cd.insuranceClaim.id = :claimId")
    long countByClaimId(@Param("claimId") UUID claimId);

    void deleteByInsuranceClaimId(UUID insuranceClaimId);
}





