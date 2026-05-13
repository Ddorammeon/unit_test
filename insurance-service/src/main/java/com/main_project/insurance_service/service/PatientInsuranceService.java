package com.main_project.insurance_service.service;

import com.main_project.insurance_service.dto.PatientInsuranceDTO;
import com.main_project.insurance_service.dto.PatientInsuranceRequestDTO;
import com.main_project.insurance_service.entity.InsurancePolicy;
import com.main_project.insurance_service.entity.PatientInsurance;
import com.main_project.insurance_service.repository.InsurancePolicyRepository;
import com.main_project.insurance_service.repository.PatientInsuranceRepository;
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
public class PatientInsuranceService implements IPatientInsuranceService {

    private final PatientInsuranceRepository patientInsuranceRepository;
    private final InsurancePolicyRepository insurancePolicyRepository;
    private final EntityDTOMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<PatientInsuranceDTO> getAllPatientInsurances() {
        return patientInsuranceRepository.findAll()
                .stream()
                .map(mapper::toPatientInsuranceDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PatientInsuranceDTO> getPatientInsuranceById(UUID id) {
        return patientInsuranceRepository.findById(id)
                .map(mapper::toPatientInsuranceDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PatientInsuranceDTO> getPatientInsuranceByPatientId(UUID patientId) {
        return patientInsuranceRepository.findByPatientId(patientId)
                .map(mapper::toPatientInsuranceDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientInsuranceDTO> getPatientInsurancesByStatus(String status) {
        return patientInsuranceRepository.findByStatus(status)
                .stream()
                .map(mapper::toPatientInsuranceDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientInsuranceDTO> getPatientInsurancesByPolicyId(UUID policyId) {
        return patientInsuranceRepository.findByInsurancePolicyId(policyId)
                .stream()
                .map(mapper::toPatientInsuranceDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientInsuranceDTO> getActiveInsurances() {
        return patientInsuranceRepository.findActiveInsurances(LocalDate.now())
                .stream()
                .map(mapper::toPatientInsuranceDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientInsuranceDTO> getExpiredInsurances() {
        return patientInsuranceRepository.findExpiredInsurances(LocalDate.now())
                .stream()
                .map(mapper::toPatientInsuranceDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PatientInsuranceDTO> getActiveInsuranceByPatientId(UUID patientId) {
        return patientInsuranceRepository.findActiveInsuranceByPatientId(patientId)
                .map(mapper::toPatientInsuranceDTO);
    }

    @Override
    public PatientInsuranceDTO createPatientInsurance(PatientInsuranceRequestDTO requestDTO) {
        if (patientInsuranceRepository.existsByPatientId(requestDTO.getPatientId())) {
            throw new IllegalArgumentException("Patient already has insurance: " + requestDTO.getPatientId());
        }

        InsurancePolicy insurancePolicy = insurancePolicyRepository.findById(requestDTO.getInsurancePolicyId())
                .orElseThrow(() -> new RuntimeException("Insurance policy not found with id: " + requestDTO.getInsurancePolicyId()));

        PatientInsurance entity = mapper.toPatientInsuranceEntity(requestDTO, insurancePolicy);
        PatientInsurance savedEntity = patientInsuranceRepository.save(entity);
        return mapper.toPatientInsuranceDTO(savedEntity);
    }

    @Override
    public PatientInsuranceDTO updatePatientInsurance(UUID id, PatientInsuranceRequestDTO requestDTO) {
        PatientInsurance existingEntity = patientInsuranceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient insurance not found with id: " + id));

        // Check if patient ID is being changed and if it already exists
        if (!existingEntity.getPatientId().equals(requestDTO.getPatientId()) &&
            patientInsuranceRepository.existsByPatientId(requestDTO.getPatientId())) {
            throw new IllegalArgumentException("Patient already has insurance: " + requestDTO.getPatientId());
        }

        InsurancePolicy insurancePolicy = insurancePolicyRepository.findById(requestDTO.getInsurancePolicyId())
                .orElseThrow(() -> new RuntimeException("Insurance policy not found with id: " + requestDTO.getInsurancePolicyId()));

        mapper.updatePatientInsuranceEntity(existingEntity, requestDTO, insurancePolicy);
        PatientInsurance updatedEntity = patientInsuranceRepository.save(existingEntity);
        return mapper.toPatientInsuranceDTO(updatedEntity);
    }

    @Override
    public void deletePatientInsurance(UUID id) {
        if (!patientInsuranceRepository.existsById(id)) {
            throw new RuntimeException("Patient insurance not found with id: " + id);
        }
        patientInsuranceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPatientId(UUID patientId) {
        return patientInsuranceRepository.existsByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isValidInsurance(UUID patientId) {
        Optional<PatientInsuranceDTO> optInsurance = getActiveInsuranceByPatientId(patientId);
        if (optInsurance.isEmpty()) {
            return false;
        }
        
        PatientInsuranceDTO patientInsurance = optInsurance.get();
        LocalDate today = LocalDate.now();
        return "ACTIVE".equals(patientInsurance.getStatus()) &&
                patientInsurance.getExpiryDate() != null &&
                !today.isAfter(patientInsurance.getExpiryDate());
    }
}





