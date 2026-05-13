package com.main_project.insurance_service.service;

import com.main_project.insurance_service.dto.BhytCatalogueDTO;
import com.main_project.insurance_service.dto.BhytCatalogueRequestDTO;
import com.main_project.insurance_service.entity.BhytCatalogue;
import com.main_project.insurance_service.repository.BhytCatalogueRepository;
import com.main_project.insurance_service.util.EntityDTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BhytCatalogueService implements IBhytCatalogueService {

    @Autowired
    private BhytCatalogueRepository bhytCatalogueRepository;

    @Autowired
    private EntityDTOMapper mapper;

    @Override
    public BhytCatalogueDTO createBhytCatalogue(BhytCatalogueRequestDTO requestDTO) {
        BhytCatalogue entity = mapper.toBhytCatalogueEntity(requestDTO);
        BhytCatalogue savedEntity = bhytCatalogueRepository.save(entity);
        return mapper.toBhytCatalogueDTO(savedEntity);
    }

    @Override
    public BhytCatalogueDTO getBhytCatalogueById(UUID id) {
        BhytCatalogue entity = bhytCatalogueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BHYT Catalogue not found with id: " + id));
        return mapper.toBhytCatalogueDTO(entity);
    }

    @Override
    public BhytCatalogueDTO getBhytCatalogueByServiceCode(String serviceCode) {
        BhytCatalogue entity = bhytCatalogueRepository.findByServiceCode(serviceCode)
                .orElseThrow(() -> new RuntimeException("BHYT Catalogue not found with service code: " + serviceCode));
        return mapper.toBhytCatalogueDTO(entity);
    }

    @Override
    public List<BhytCatalogueDTO> getAllBhytCatalogues() {
        return bhytCatalogueRepository.findAll().stream()
                .map(mapper::toBhytCatalogueDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BhytCatalogueDTO updateBhytCatalogue(UUID id, BhytCatalogueRequestDTO requestDTO) {
        BhytCatalogue entity = bhytCatalogueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BHYT Catalogue not found with id: " + id));
        mapper.updateBhytCatalogueEntity(entity, requestDTO);
        BhytCatalogue updatedEntity = bhytCatalogueRepository.save(entity);
        return mapper.toBhytCatalogueDTO(updatedEntity);
    }

    @Override
    public void deleteBhytCatalogue(UUID id) {
        if (!bhytCatalogueRepository.existsById(id)) {
            throw new RuntimeException("BHYT Catalogue not found with id: " + id);
        }
        bhytCatalogueRepository.deleteById(id);
    }
}

