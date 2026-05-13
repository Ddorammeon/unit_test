package com.main_project.insurance_service.service;

import com.main_project.insurance_service.dto.ClaimItemDTO;
import com.main_project.insurance_service.dto.ClaimItemRequestDTO;

import java.util.List;
import java.util.UUID;

public interface IClaimItemService {
    ClaimItemDTO createClaimItem(ClaimItemRequestDTO requestDTO);
    ClaimItemDTO getClaimItemById(UUID id);
    List<ClaimItemDTO> getAllClaimItems();
    List<ClaimItemDTO> getClaimItemsByClaimId(UUID claimId);
    ClaimItemDTO updateClaimItem(UUID id, ClaimItemRequestDTO requestDTO);
    void deleteClaimItem(UUID id);
}

