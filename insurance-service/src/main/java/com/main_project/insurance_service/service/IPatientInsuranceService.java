package com.main_project.insurance_service.service;

import com.main_project.insurance_service.dto.PatientInsuranceDTO;
import com.main_project.insurance_service.dto.PatientInsuranceRequestDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IPatientInsuranceService {
    
    List<PatientInsuranceDTO> getAllPatientInsurances();
    
    Optional<PatientInsuranceDTO> getPatientInsuranceById(UUID id);
    
    Optional<PatientInsuranceDTO> getPatientInsuranceByPatientId(UUID patientId);
    
    List<PatientInsuranceDTO> getPatientInsurancesByStatus(String status);
    
    List<PatientInsuranceDTO> getPatientInsurancesByPolicyId(UUID policyId);
    
    List<PatientInsuranceDTO> getActiveInsurances();
    
    List<PatientInsuranceDTO> getExpiredInsurances();
    
    Optional<PatientInsuranceDTO> getActiveInsuranceByPatientId(UUID patientId);
    
    PatientInsuranceDTO createPatientInsurance(PatientInsuranceRequestDTO requestDTO);
    
    PatientInsuranceDTO updatePatientInsurance(UUID id, PatientInsuranceRequestDTO requestDTO);
    
    void deletePatientInsurance(UUID id);
    
    boolean existsByPatientId(UUID patientId);
    
    boolean isValidInsurance(UUID patientId);
}
