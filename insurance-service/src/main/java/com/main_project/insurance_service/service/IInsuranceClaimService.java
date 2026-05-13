package com.main_project.insurance_service.service;
import com.do_an.common.model.InvoiceCheckerRequest;
import com.do_an.common.model.InvoiceItemResponse;
import com.main_project.insurance_service.dto.ClaimItemRequestDTO;
import com.main_project.insurance_service.dto.InsuranceClaimDTO;
import com.main_project.insurance_service.dto.InsuranceClaimRequestDTO;
import com.main_project.insurance_service.dto.InvoiceDTO;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface IInsuranceClaimService {
    
    List<InsuranceClaimDTO> getAllClaims();
    
    Optional<InsuranceClaimDTO> getClaimById(UUID id);
    
    List<InsuranceClaimDTO> getClaimsByStatus(String status);
    
    List<InsuranceClaimDTO> getClaimsByPatientInsuranceId(UUID patientInsuranceId);
    
    List<InsuranceClaimDTO> getClaimsByPatientId(UUID patientId);
    
    List<InsuranceClaimDTO> getClaimsByDateRange(ZonedDateTime startDate, ZonedDateTime endDate);
    
    List<InsuranceClaimDTO> getClaimsByStatusAndMinAmount(String status, Integer minAmount);
    
    long getClaimCountByPatientInsurance(UUID patientInsuranceId);
    
    Integer getTotalApprovedAmountByPatientInsurance(UUID patientInsuranceId);
    
    InsuranceClaimDTO createClaim(InsuranceClaimRequestDTO requestDTO);

    InvoiceDTO processInvoiceClaim(InvoiceCheckerRequest requestDTO);

    InsuranceClaimDTO updateClaim(UUID id, InsuranceClaimRequestDTO requestDTO);
    
    InsuranceClaimDTO approveClaim(UUID id, Integer approvedAmount);
    
    InsuranceClaimDTO rejectClaim(UUID id, String reason);
    
    void deleteClaim(UUID id);
    
    Set<InvoiceItemResponse> processInvoiceItemsForValidation(InvoiceCheckerRequest request, UUID patientId);
    
    InsuranceClaimDTO createClaimFromValidationEvent(UUID claimId, UUID patientId, Set<InvoiceItemResponse> items);
    
    ClaimItemRequestDTO createClaimItemRequestFromInvoiceItem(InvoiceItemResponse item, UUID claimId);
}





