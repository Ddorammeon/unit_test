package com.main_project.appointment_service.service;

import com.main_project.appointment_service.dto.MedicalServiceDTO;
import com.main_project.appointment_service.dto.MedicalServiceRequestDTO;
import com.main_project.appointment_service.entity.MedicalService;
import com.main_project.appointment_service.enums.MedicalServiceStatus;
import com.main_project.appointment_service.repository.MedicalServiceRepository;
import com.main_project.appointment_service.util.EntityDTOMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalServiceService implements IMedicalService {

    private final MedicalServiceRepository repository;
    private final EntityDTOMapper mapper;

    @Override
    public List<MedicalServiceDTO> getAllMedicalServices() {
        return repository.findAll()
                .stream()
                .map(mapper::toMedicalServiceDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<MedicalServiceDTO> getMedicalServiceById(UUID id) {
        return repository.findById(id).map(mapper::toMedicalServiceDTO);
    }

    @Override
    public List<MedicalServiceDTO> getMedicalServicesByName(String name) {
        return repository.findByServiceName(name)
                .stream()
                .map(mapper::toMedicalServiceDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MedicalServiceDTO> searchMedicalServicesByName(String keyword) {
        return repository.searchByServiceName(keyword)
                .stream()
                .map(mapper::toMedicalServiceDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MedicalServiceDTO> getMedicalServicesByType(String type) {
        return repository.findByServiceType(type)
                .stream()
                .map(mapper::toMedicalServiceDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long countByServiceType(String type) {
        return repository.countByServiceType(type);
    }

    @Override
    public long countByServiceName(String name) {
        return repository.countByServiceName(name);
    }

    @Override
    public MedicalServiceDTO createMedicalService(MedicalServiceRequestDTO requestDTO) {
        MedicalService entity = mapper.toMedicalServiceEntity(requestDTO);
        entity = repository.save(entity);
        return mapper.toMedicalServiceDTO(entity);
    }

    @Override
    public MedicalServiceDTO updateMedicalService(UUID id, MedicalServiceRequestDTO requestDTO) {
        MedicalService existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical Service not found"));
        mapper.updateMedicalServiceEntity(existing, requestDTO);
        existing = repository.save(existing);
        return mapper.toMedicalServiceDTO(existing);
    }

    @Override
    public int deactivateMedicalServiceName(String name) {
        int updatedRows = repository.deactivateMedicalServiceName(name);

        if (updatedRows == 0) {
            throw new RuntimeException("Không tìm thấy dịch vụ với tên: " + name);
        }

        return updatedRows;
    }

    @Override
    public int deactivateMedicalService(UUID id) {
        Optional<MedicalService> optional = repository.findById(id);

        if (optional.isEmpty()) {
            throw new RuntimeException("Không tìm thấy dịch vụ với ID: " + id);
        }

        MedicalService service = optional.get();
        service.setStatus(MedicalServiceStatus.INACTIVE);

        repository.save(service);

        return 1;
    }

    @Override
    public int deactivateMedicalServiceType(String type) {
        int updatedRows = repository.deactivateMedicalServiceType(type);

        if (updatedRows == 0) {
            throw new RuntimeException("Không tìm thấy dịch vụ theo loại: " + type);
        }

        return updatedRows;
    }

    @Override
    @Transactional
    public void deleteByServiceType(String type) {
        repository.deleteByServiceType(type);
    }

    @Override
    @Transactional
    public void deleteByServiceName(String name) {
        repository.deleteByServiceName(name);
    }

    @Override
    public void deleteMedicalService(UUID id) {
        repository.deleteById(id);
    }
}
