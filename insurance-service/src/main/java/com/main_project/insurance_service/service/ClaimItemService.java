package com.main_project.insurance_service.service;

import com.main_project.insurance_service.dto.ClaimItemDTO;
import com.main_project.insurance_service.dto.ClaimItemRequestDTO;
import com.main_project.insurance_service.entity.BhytCatalogue;
import com.main_project.insurance_service.entity.ClaimItem;
import com.main_project.insurance_service.entity.InsuranceClaim;
import com.main_project.insurance_service.repository.BhytCatalogueRepository;
import com.main_project.insurance_service.repository.ClaimItemRepository;
import com.main_project.insurance_service.repository.InsuranceClaimRepository;
import com.main_project.insurance_service.util.EntityDTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClaimItemService implements IClaimItemService {

    @Autowired
    private ClaimItemRepository claimItemRepository;

    @Autowired
    private InsuranceClaimRepository insuranceClaimRepository;

    @Autowired
    private BhytCatalogueRepository bhytCatalogueRepository;

    @Autowired
    private EntityDTOMapper mapper;

    @Override
    public ClaimItemDTO createClaimItem(ClaimItemRequestDTO requestDTO) {
        InsuranceClaim insuranceClaim = insuranceClaimRepository.findById(requestDTO.getInsuranceClaimId())
                .orElseThrow(() -> new RuntimeException("Insurance Claim not found with id: " + requestDTO.getInsuranceClaimId()));
        
        BhytCatalogue bhytCatalogue = bhytCatalogueRepository.findById(requestDTO.getBhytCatalogueId())
                .orElseThrow(() -> new RuntimeException("BHYT Catalogue not found with id: " + requestDTO.getBhytCatalogueId()));
        
        ClaimItem entity = mapper.toClaimItemEntity(requestDTO, insuranceClaim, bhytCatalogue);
        ClaimItem savedEntity = claimItemRepository.save(entity);
        return mapper.toClaimItemDTO(savedEntity);
    }

    @Override
    public ClaimItemDTO getClaimItemById(UUID id) {
        ClaimItem entity = claimItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim Item not found with id: " + id));
        return mapper.toClaimItemDTO(entity);
    }

    @Override
    public List<ClaimItemDTO> getAllClaimItems() {
        return claimItemRepository.findAll().stream()
                .map(mapper::toClaimItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClaimItemDTO> getClaimItemsByClaimId(UUID claimId) {
        return claimItemRepository.findByInsuranceClaimId(claimId).stream()
                .map(mapper::toClaimItemDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ClaimItemDTO updateClaimItem(UUID id, ClaimItemRequestDTO requestDTO) {
        ClaimItem entity = claimItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim Item not found with id: " + id));
        
        InsuranceClaim insuranceClaim = insuranceClaimRepository.findById(requestDTO.getInsuranceClaimId())
                .orElseThrow(() -> new RuntimeException("Insurance Claim not found with id: " + requestDTO.getInsuranceClaimId()));
        
        BhytCatalogue bhytCatalogue = bhytCatalogueRepository.findById(requestDTO.getBhytCatalogueId())
                .orElseThrow(() -> new RuntimeException("BHYT Catalogue not found with id: " + requestDTO.getBhytCatalogueId()));
        
        mapper.updateClaimItemEntity(entity, requestDTO, insuranceClaim, bhytCatalogue);
        ClaimItem updatedEntity = claimItemRepository.save(entity);
        return mapper.toClaimItemDTO(updatedEntity);
    }

    @Override
    public void deleteClaimItem(UUID id) {
        if (!claimItemRepository.existsById(id)) {
            throw new RuntimeException("Claim Item not found with id: " + id);
        }
        claimItemRepository.deleteById(id);
    }
}

