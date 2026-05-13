package com.main_project.insurance_service.service;

import com.main_project.insurance_service.dto.InsurancePolicyDTO;
import com.main_project.insurance_service.dto.InsurancePolicyRequestDTO;
import com.main_project.insurance_service.entity.InsurancePolicy;
import com.main_project.insurance_service.repository.InsurancePolicyRepository;
import com.main_project.insurance_service.util.EntityDTOMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InsurancePolicyService implements IInsurancePolicyService {

    private final InsurancePolicyRepository insurancePolicyRepository;
    private final EntityDTOMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<InsurancePolicyDTO> getAllPolicies() {
        return insurancePolicyRepository.findAll()
                .stream()
                .map(mapper::toInsurancePolicyDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InsurancePolicyDTO> getPolicyById(UUID id) {
        return insurancePolicyRepository.findById(id)
                .map(mapper::toInsurancePolicyDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InsurancePolicyDTO> getPolicyByPolicyNumber(String policyNumber) {
        return insurancePolicyRepository.findByPolicyNumber(policyNumber)
                .map(mapper::toInsurancePolicyDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsurancePolicyDTO> getPoliciesByType(String policyType) {
        return insurancePolicyRepository.findByPolicyType(policyType)
                .stream()
                .map(mapper::toInsurancePolicyDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsurancePolicyDTO> getPoliciesByStatus(String status) {
        return insurancePolicyRepository.findByStatus(status)
                .stream()
                .map(mapper::toInsurancePolicyDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsurancePolicyDTO> getPoliciesByCoverageAmount(Integer minAmount) {
        return insurancePolicyRepository.findByCoverageAmountGreaterThanEqual(minAmount)
                .stream()
                .map(mapper::toInsurancePolicyDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsurancePolicyDTO> getPoliciesByTypeAndStatus(String policyType, String status) {
        return insurancePolicyRepository.findByPolicyTypeAndStatus(policyType, status)
                .stream()
                .map(mapper::toInsurancePolicyDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InsurancePolicyDTO createPolicy(InsurancePolicyRequestDTO requestDTO) {
        if (insurancePolicyRepository.existsByPolicyNumber(requestDTO.getPolicyNumber())) {
            throw new IllegalArgumentException("Policy number already exists: " + requestDTO.getPolicyNumber());
        }

        InsurancePolicy entity = mapper.toInsurancePolicyEntity(requestDTO);
        InsurancePolicy savedEntity = insurancePolicyRepository.save(entity);
        return mapper.toInsurancePolicyDTO(savedEntity);
    }

    @Override
    public InsurancePolicyDTO updatePolicy(UUID id, InsurancePolicyRequestDTO requestDTO) {
        InsurancePolicy existingEntity = insurancePolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insurance policy not found with id: " + id));

        // Check if policy number is being changed and if it already exists
        if (!existingEntity.getPolicyNumber().equals(requestDTO.getPolicyNumber()) &&
            insurancePolicyRepository.existsByPolicyNumber(requestDTO.getPolicyNumber())) {
            throw new IllegalArgumentException("Policy number already exists: " + requestDTO.getPolicyNumber());
        }

        mapper.updateInsurancePolicyEntity(existingEntity, requestDTO);
        InsurancePolicy updatedEntity = insurancePolicyRepository.save(existingEntity);
        return mapper.toInsurancePolicyDTO(updatedEntity);
    }

    @Override
    public void deletePolicy(UUID id) {
        if (!insurancePolicyRepository.existsById(id)) {
            throw new RuntimeException("Insurance policy not found with id: " + id);
        }
        insurancePolicyRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPolicyNumber(String policyNumber) {
        return insurancePolicyRepository.existsByPolicyNumber(policyNumber);
    }
}





