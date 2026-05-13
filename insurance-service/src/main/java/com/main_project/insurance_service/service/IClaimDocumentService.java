package com.main_project.insurance_service.service;

import com.main_project.insurance_service.dto.ClaimDocumentDTO;
import com.main_project.insurance_service.dto.ClaimDocumentRequestDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IClaimDocumentService {
    
    List<ClaimDocumentDTO> getAllDocuments();
    
    Optional<ClaimDocumentDTO> getDocumentById(UUID id);
    
    List<ClaimDocumentDTO> getDocumentsByClaimId(UUID claimId);
    
    List<ClaimDocumentDTO> getDocumentsByDocumentType(String documentType);
    
    List<ClaimDocumentDTO> getDocumentsByStatus(String status);
    
    List<ClaimDocumentDTO> getDocumentsByClaimIdAndType(UUID claimId, String documentType);
    
    List<ClaimDocumentDTO> getDocumentsByPatientId(UUID patientId);
    
    long getDocumentCountByClaimId(UUID claimId);
    
    ClaimDocumentDTO createDocument(ClaimDocumentRequestDTO requestDTO);
    
    ClaimDocumentDTO updateDocument(UUID id, ClaimDocumentRequestDTO requestDTO);
    
    ClaimDocumentDTO updateDocumentStatus(UUID id, String status);
    
    void deleteDocument(UUID id);
    
    void deleteDocumentsByClaimId(UUID claimId);
}





