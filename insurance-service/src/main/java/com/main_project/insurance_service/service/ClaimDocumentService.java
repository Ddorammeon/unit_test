package com.main_project.insurance_service.service;

import com.main_project.insurance_service.dto.ClaimDocumentDTO;
import com.main_project.insurance_service.dto.ClaimDocumentRequestDTO;
import com.main_project.insurance_service.entity.ClaimDocument;
import com.main_project.insurance_service.entity.InsuranceClaim;
import com.main_project.insurance_service.repository.ClaimDocumentRepository;
import com.main_project.insurance_service.repository.InsuranceClaimRepository;
import com.main_project.insurance_service.util.EntityDTOMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClaimDocumentService implements IClaimDocumentService {

    private final ClaimDocumentRepository claimDocumentRepository;
    private final InsuranceClaimRepository insuranceClaimRepository;
    private final EntityDTOMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ClaimDocumentDTO> getAllDocuments() {
        return claimDocumentRepository.findAll()
                .stream()
                .map(mapper::toClaimDocumentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClaimDocumentDTO> getDocumentById(UUID id) {
        return claimDocumentRepository.findById(id)
                .map(mapper::toClaimDocumentDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimDocumentDTO> getDocumentsByClaimId(UUID claimId) {
        return claimDocumentRepository.findByInsuranceClaimId(claimId)
                .stream()
                .map(mapper::toClaimDocumentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimDocumentDTO> getDocumentsByDocumentType(String documentType) {
        return claimDocumentRepository.findByDocumentType(documentType)
                .stream()
                .map(mapper::toClaimDocumentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimDocumentDTO> getDocumentsByStatus(String status) {
        return claimDocumentRepository.findByStatus(status)
                .stream()
                .map(mapper::toClaimDocumentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimDocumentDTO> getDocumentsByClaimIdAndType(UUID claimId, String documentType) {
        return claimDocumentRepository.findByClaimIdAndDocumentType(claimId, documentType)
                .stream()
                .map(mapper::toClaimDocumentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimDocumentDTO> getDocumentsByPatientId(UUID patientId) {
        return claimDocumentRepository.findByPatientId(patientId)
                .stream()
                .map(mapper::toClaimDocumentDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getDocumentCountByClaimId(UUID claimId) {
        return claimDocumentRepository.countByClaimId(claimId);
    }

    @Override
    public ClaimDocumentDTO createDocument(ClaimDocumentRequestDTO requestDTO) {
        InsuranceClaim insuranceClaim = insuranceClaimRepository.findById(requestDTO.getInsuranceClaimId())
                .orElseThrow(() -> new RuntimeException("Insurance claim not found with id: " + requestDTO.getInsuranceClaimId()));

        ClaimDocument entity = mapper.toClaimDocumentEntity(requestDTO, insuranceClaim);
        ClaimDocument savedEntity = claimDocumentRepository.save(entity);
        return mapper.toClaimDocumentDTO(savedEntity);
    }

    @Override
    public ClaimDocumentDTO updateDocument(UUID id, ClaimDocumentRequestDTO requestDTO) {
        ClaimDocument existingEntity = claimDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim document not found with id: " + id));

        InsuranceClaim insuranceClaim = insuranceClaimRepository.findById(requestDTO.getInsuranceClaimId())
                .orElseThrow(() -> new RuntimeException("Insurance claim not found with id: " + requestDTO.getInsuranceClaimId()));

        mapper.updateClaimDocumentEntity(existingEntity, requestDTO, insuranceClaim);
        ClaimDocument updatedEntity = claimDocumentRepository.save(existingEntity);
        return mapper.toClaimDocumentDTO(updatedEntity);
    }

    @Override
    public ClaimDocumentDTO updateDocumentStatus(UUID id, String status) {
        ClaimDocument existingEntity = claimDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim document not found with id: " + id));

        existingEntity.setStatus(status);
        ClaimDocument updatedEntity = claimDocumentRepository.save(existingEntity);
        return mapper.toClaimDocumentDTO(updatedEntity);
    }

    @Override
    public void deleteDocument(UUID id) {
        if (!claimDocumentRepository.existsById(id)) {
            throw new RuntimeException("Claim document not found with id: " + id);
        }
        claimDocumentRepository.deleteById(id);
    }

    @Override
    public void deleteDocumentsByClaimId(UUID claimId) {
        claimDocumentRepository.deleteByInsuranceClaimId(claimId);
    }
}





