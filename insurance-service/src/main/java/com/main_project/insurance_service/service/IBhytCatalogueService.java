package com.main_project.insurance_service.service;

import com.main_project.insurance_service.dto.BhytCatalogueDTO;
import com.main_project.insurance_service.dto.BhytCatalogueRequestDTO;

import java.util.List;
import java.util.UUID;

public interface IBhytCatalogueService {
    BhytCatalogueDTO createBhytCatalogue(BhytCatalogueRequestDTO requestDTO);
    BhytCatalogueDTO getBhytCatalogueById(UUID id);
    BhytCatalogueDTO getBhytCatalogueByServiceCode(String serviceCode);
    List<BhytCatalogueDTO> getAllBhytCatalogues();
    BhytCatalogueDTO updateBhytCatalogue(UUID id, BhytCatalogueRequestDTO requestDTO);
    void deleteBhytCatalogue(UUID id);
}

