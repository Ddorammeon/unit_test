package com.main_project.insurance_service.controller;

import com.main_project.insurance_service.dto.InsurancePolicyDTO;
import com.main_project.insurance_service.dto.InsurancePolicyRequestDTO;
import com.main_project.insurance_service.service.IInsurancePolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/insurance-service/insurance-policies")
@RequiredArgsConstructor
@Tag(name = "Insurance Policy", description = "Insurance Policy Management APIs")
public class InsurancePolicyController {

    private final IInsurancePolicyService insurancePolicyService;

    @GetMapping
    @Operation(summary = "Get all insurance policies", description = "Retrieve all insurance policies")
    public ResponseEntity<List<InsurancePolicyDTO>> getAllPolicies() {
        List<InsurancePolicyDTO> policies = insurancePolicyService.getAllPolicies();
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get insurance policy by ID", description = "Retrieve a specific insurance policy by its ID")
    public ResponseEntity<InsurancePolicyDTO> getPolicyById(
            @Parameter(description = "Policy ID") @PathVariable UUID id) {
        Optional<InsurancePolicyDTO> policy = insurancePolicyService.getPolicyById(id);
        return policy.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/policy-number/{policyNumber}")
    @Operation(summary = "Get insurance policy by policy number", description = "Retrieve a specific insurance policy by policy number")
    public ResponseEntity<InsurancePolicyDTO> getPolicyByPolicyNumber(
            @Parameter(description = "Policy Number") @PathVariable String policyNumber) {
        Optional<InsurancePolicyDTO> policy = insurancePolicyService.getPolicyByPolicyNumber(policyNumber);
        return policy.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{policyType}")
    @Operation(summary = "Get policies by type", description = "Retrieve insurance policies by policy type")
    public ResponseEntity<List<InsurancePolicyDTO>> getPoliciesByType(
            @Parameter(description = "Policy Type") @PathVariable String policyType) {
        List<InsurancePolicyDTO> policies = insurancePolicyService.getPoliciesByType(policyType);
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get policies by status", description = "Retrieve insurance policies by status")
    public ResponseEntity<List<InsurancePolicyDTO>> getPoliciesByStatus(
            @Parameter(description = "Policy Status") @PathVariable String status) {
        List<InsurancePolicyDTO> policies = insurancePolicyService.getPoliciesByStatus(status);
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/coverage-amount/{minAmount}")
    @Operation(summary = "Get policies by minimum coverage amount", description = "Retrieve insurance policies with coverage amount >= specified minimum")
    public ResponseEntity<List<InsurancePolicyDTO>> getPoliciesByCoverageAmount(
            @Parameter(description = "Minimum Coverage Amount") @PathVariable Integer minAmount) {
        List<InsurancePolicyDTO> policies = insurancePolicyService.getPoliciesByCoverageAmount(minAmount);
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/search")
    @Operation(summary = "Search policies by type and status", description = "Search insurance policies by policy type and status")
    public ResponseEntity<List<InsurancePolicyDTO>> getPoliciesByTypeAndStatus(
            @Parameter(description = "Policy Type") @RequestParam String policyType,
            @Parameter(description = "Policy Status") @RequestParam String status) {
        List<InsurancePolicyDTO> policies = insurancePolicyService.getPoliciesByTypeAndStatus(policyType, status);
        return ResponseEntity.ok(policies);
    }

    @PostMapping
    @Operation(summary = "Create new insurance policy", description = "Create a new insurance policy")
    public ResponseEntity<InsurancePolicyDTO> createPolicy(
            @Valid @RequestBody InsurancePolicyRequestDTO requestDTO) {
        try {
            InsurancePolicyDTO createdPolicy = insurancePolicyService.createPolicy(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPolicy);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update insurance policy", description = "Update an existing insurance policy")
    public ResponseEntity<InsurancePolicyDTO> updatePolicy(
            @Parameter(description = "Policy ID") @PathVariable UUID id,
            @Valid @RequestBody InsurancePolicyRequestDTO requestDTO) {
        try {
            InsurancePolicyDTO updatedPolicy = insurancePolicyService.updatePolicy(id, requestDTO);
            return ResponseEntity.ok(updatedPolicy);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete insurance policy", description = "Delete an insurance policy")
    public ResponseEntity<Void> deletePolicy(
            @Parameter(description = "Policy ID") @PathVariable UUID id) {
        try {
            insurancePolicyService.deletePolicy(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/exists/{policyNumber}")
    @Operation(summary = "Check if policy number exists", description = "Check if a policy number already exists")
    public ResponseEntity<Boolean> existsByPolicyNumber(
            @Parameter(description = "Policy Number") @PathVariable String policyNumber) {
        boolean exists = insurancePolicyService.existsByPolicyNumber(policyNumber);
        return ResponseEntity.ok(exists);
    }
}
