package com.main_project.insurance_service.controller;

import com.do_an.common.model.InvoiceCheckerRequest;
import com.main_project.insurance_service.dto.InsuranceClaimDTO;
import com.main_project.insurance_service.dto.InsuranceClaimRequestDTO;
import com.main_project.insurance_service.dto.InvoiceDTO;
import com.main_project.insurance_service.service.IInsuranceClaimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/insurance-service/insurance-claims")
@RequiredArgsConstructor
@Tag(name = "Insurance Claim", description = "Insurance Claim Management APIs")
public class InsuranceClaimController {

    private final IInsuranceClaimService insuranceClaimService;

    @GetMapping
    @Operation(summary = "Get all insurance claims", description = "Retrieve all insurance claim records")
    public ResponseEntity<List<InsuranceClaimDTO>> getAllClaims() {
        List<InsuranceClaimDTO> claims = insuranceClaimService.getAllClaims();
        return ResponseEntity.ok(claims);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get insurance claim by ID", description = "Retrieve a specific insurance claim by its ID")
    public ResponseEntity<InsuranceClaimDTO> getClaimById(
            @Parameter(description = "Claim ID") @PathVariable UUID id) {
        Optional<InsuranceClaimDTO> claim = insuranceClaimService.getClaimById(id);
        return claim.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get claims by status", description = "Retrieve insurance claims by status")
    public ResponseEntity<List<InsuranceClaimDTO>> getClaimsByStatus(
            @Parameter(description = "Claim Status") @PathVariable String status) {
        List<InsuranceClaimDTO> claims = insuranceClaimService.getClaimsByStatus(status);
        return ResponseEntity.ok(claims);
    }

    @GetMapping("/patient-insurance/{patientInsuranceId}")
    @Operation(summary = "Get claims by patient insurance ID", description = "Retrieve insurance claims by patient insurance ID")
    public ResponseEntity<List<InsuranceClaimDTO>> getClaimsByPatientInsuranceId(
            @Parameter(description = "Patient Insurance ID") @PathVariable UUID patientInsuranceId) {
        List<InsuranceClaimDTO> claims = insuranceClaimService.getClaimsByPatientInsuranceId(patientInsuranceId);
        return ResponseEntity.ok(claims);
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get claims by patient ID", description = "Retrieve insurance claims by patient ID")
    public ResponseEntity<List<InsuranceClaimDTO>> getClaimsByPatientId(
            @Parameter(description = "Patient ID") @PathVariable UUID patientId) {
        List<InsuranceClaimDTO> claims = insuranceClaimService.getClaimsByPatientId(patientId);
        return ResponseEntity.ok(claims);
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get claims by date range", description = "Retrieve insurance claims within a date range")
    public ResponseEntity<List<InsuranceClaimDTO>> getClaimsByDateRange(
            @Parameter(description = "Start Date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @Parameter(description = "End Date") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate) {
        List<InsuranceClaimDTO> claims = insuranceClaimService.getClaimsByDateRange(startDate, endDate);
        return ResponseEntity.ok(claims);
    }

    @GetMapping("/search")
    @Operation(summary = "Search claims by status and minimum amount", description = "Search insurance claims by status and minimum total claim amount")
    public ResponseEntity<List<InsuranceClaimDTO>> getClaimsByStatusAndMinAmount(
            @Parameter(description = "Claim Status") @RequestParam String status,
            @Parameter(description = "Minimum Total Claim Amount") @RequestParam Integer minAmount) {
        List<InsuranceClaimDTO> claims = insuranceClaimService.getClaimsByStatusAndMinAmount(status, minAmount);
        return ResponseEntity.ok(claims);
    }

    @GetMapping("/count/patient-insurance/{patientInsuranceId}")
    @Operation(summary = "Get claim count by patient insurance", description = "Get the number of claims for a patient insurance")
    public ResponseEntity<Long> getClaimCountByPatientInsurance(
            @Parameter(description = "Patient Insurance ID") @PathVariable UUID patientInsuranceId) {
        long count = insuranceClaimService.getClaimCountByPatientInsurance(patientInsuranceId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/total-approved/patient-insurance/{patientInsuranceId}")
    @Operation(summary = "Get total insurance pay amount by patient insurance", description = "Get the total insurance pay amount for a patient insurance (sum of totalInsurancePay)")
    public ResponseEntity<Integer> getTotalApprovedAmountByPatientInsurance(
            @Parameter(description = "Patient Insurance ID") @PathVariable UUID patientInsuranceId) {
        Integer totalAmount = insuranceClaimService.getTotalApprovedAmountByPatientInsurance(patientInsuranceId);
        return ResponseEntity.ok(totalAmount);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update insurance claim", description = "Update an existing insurance claim")
    public ResponseEntity<InsuranceClaimDTO> updateClaim(
            @Parameter(description = "Claim ID") @PathVariable UUID id,
            @Valid @RequestBody InsuranceClaimRequestDTO requestDTO) {
        try {
            InsuranceClaimDTO updatedClaim = insuranceClaimService.updateClaim(id, requestDTO);
            return ResponseEntity.ok(updatedClaim);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Approve insurance claim", description = "Approve an insurance claim (approved amount tracked via totalInsurancePay)")
    public ResponseEntity<InsuranceClaimDTO> approveClaim(
            @Parameter(description = "Claim ID") @PathVariable UUID id,
            @Parameter(description = "Approved Amount (deprecated - not used)") @RequestParam(required = false) Integer approvedAmount) {
        try {
            InsuranceClaimDTO approvedClaim = insuranceClaimService.approveClaim(id, approvedAmount);
            return ResponseEntity.ok(approvedClaim);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Reject insurance claim", description = "Reject an insurance claim with reason")
    public ResponseEntity<InsuranceClaimDTO> rejectClaim(
            @Parameter(description = "Claim ID") @PathVariable UUID id,
            @Parameter(description = "Rejection Reason") @RequestParam String reason) {
        try {
            InsuranceClaimDTO rejectedClaim = insuranceClaimService.rejectClaim(id, reason);
            return ResponseEntity.ok(rejectedClaim);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete insurance claim", description = "Delete an insurance claim")
    public ResponseEntity<Void> deleteClaim(
            @Parameter(description = "Claim ID") @PathVariable UUID id) {
        try {
            insuranceClaimService.deleteClaim(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/validate-bhyt")
    @Operation(summary = "Validate invoice BHYT", description = "Validate invoice BHYT")
    public ResponseEntity<InvoiceDTO> validateBHYT(
            @Valid @RequestBody InvoiceCheckerRequest requestDTO) {
        InvoiceDTO invoiceResponse = insuranceClaimService.processInvoiceClaim(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceResponse);

    }
}





