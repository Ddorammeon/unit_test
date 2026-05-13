package com.main_project.patient_service.service;

import com.main_project.patient_service.dto.AllergyDTO;
import com.main_project.patient_service.entity.Allergy;
import com.main_project.patient_service.repository.AllergyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AllergyService - Service for Allergy Master Data
 *
 * Manages the allergy catalog used for lookup/reference by PatientAllergy.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AllergyService implements IAllergyService {

    private final AllergyRepository allergyRepository;

    @Override
    public AllergyDTO createAllergy(AllergyDTO dto) {
        Allergy allergy = Allergy.builder()
                .name(dto.getName())
                .type(dto.getType())
                .description(dto.getDescription())
                .build();

        Allergy saved = allergyRepository.save(allergy);
        return mapToDTO(saved);
    }

    @Override
    public AllergyDTO updateAllergy(UUID id, AllergyDTO dto) {
        Allergy allergy = allergyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Allergy not found with id: " + id));

        allergy.setName(dto.getName());
        allergy.setType(dto.getType());
        allergy.setDescription(dto.getDescription());

        Allergy saved = allergyRepository.save(allergy);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AllergyDTO getAllergyById(UUID id) {
        Allergy allergy = allergyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Allergy not found with id: " + id));
        return mapToDTO(allergy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllergyDTO> getAllAllergies() {
        return allergyRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAllergy(UUID id) {
        if (!allergyRepository.existsById(id)) {
            throw new EntityNotFoundException("Allergy not found with id: " + id);
        }
        allergyRepository.deleteById(id);
    }

    /**
     * Maps Allergy entity to AllergyDTO.
     */
    private AllergyDTO mapToDTO(Allergy allergy) {
        return AllergyDTO.builder()
                .id(allergy.getId())
                .name(allergy.getName())
                .type(allergy.getType())
                .description(allergy.getDescription())
                .build();
    }
}
