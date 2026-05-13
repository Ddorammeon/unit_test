package com.main_project.insurance_service.service;

import com.main_project.insurance_service.dto.InsurancePolicyDTO;
import com.main_project.insurance_service.dto.InsurancePolicyRequestDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IInsurancePolicyService {
    
    List<InsurancePolicyDTO> getAllPolicies();
    
    Optional<InsurancePolicyDTO> getPolicyById(UUID id);
    
    Optional<InsurancePolicyDTO> getPolicyByPolicyNumber(String policyNumber);
    
    List<InsurancePolicyDTO> getPoliciesByType(String policyType);
    
    List<InsurancePolicyDTO> getPoliciesByStatus(String status);
    
    List<InsurancePolicyDTO> getPoliciesByCoverageAmount(Integer minAmount);
    
    List<InsurancePolicyDTO> getPoliciesByTypeAndStatus(String policyType, String status);
    
    InsurancePolicyDTO createPolicy(InsurancePolicyRequestDTO requestDTO);
    
    InsurancePolicyDTO updatePolicy(UUID id, InsurancePolicyRequestDTO requestDTO);
    
    void deletePolicy(UUID id);
    
    boolean existsByPolicyNumber(String policyNumber);
}





