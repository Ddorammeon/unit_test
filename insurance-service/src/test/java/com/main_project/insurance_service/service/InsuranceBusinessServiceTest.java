package com.main_project.insurance_service.service;

import com.do_an.common.model.InvoiceCheckerRequest;
import com.do_an.common.model.InvoiceItemCheckerRequest;
import com.do_an.common.model.InvoiceItemResponse;
import com.main_project.insurance_service.dto.*;
import com.main_project.insurance_service.entity.*;
import com.main_project.insurance_service.repository.*;
import com.main_project.insurance_service.util.EntityDTOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsuranceBusinessServiceTest {

    @Mock
    private InsurancePolicyRepository insurancePolicyRepository;
    @Mock
    private PatientInsuranceRepository patientInsuranceRepository;
    @Mock
    private InsuranceClaimRepository insuranceClaimRepository;
    @Mock
    private ClaimDocumentRepository claimDocumentRepository;
    @Mock
    private IBhytCatalogueService bhytCatalogueService;
    @Mock
    private IClaimItemService claimItemService;
    @Mock
    private IInsurancePolicyService insurancePolicyDependency;

    private InsurancePolicyService policyService;
    private PatientInsuranceService patientInsuranceService;
    private InsuranceClaimService claimService;
    private ClaimDocumentService documentService;

    private final EntityDTOMapper mapper = new EntityDTOMapper();

    private final UUID policyId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID patientInsuranceId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID patientId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private final UUID claimId = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private final UUID documentId = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private final UUID serviceReferenceId = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private final UUID catalogueId = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private final UUID claimItemId = UUID.fromString("88888888-8888-8888-8888-888888888888");

    @BeforeEach
    void setUp() {
        policyService = new InsurancePolicyService(insurancePolicyRepository, mapper);
        patientInsuranceService = new PatientInsuranceService(patientInsuranceRepository, insurancePolicyRepository, mapper);
        claimService = new InsuranceClaimService(
                insuranceClaimRepository,
                patientInsuranceRepository,
                bhytCatalogueService,
                claimItemService,
                insurancePolicyDependency,
                mapper
        );
        documentService = new ClaimDocumentService(claimDocumentRepository, insuranceClaimRepository, mapper);

        lenient().when(insurancePolicyRepository.save(any(InsurancePolicy.class))).thenAnswer(invocation -> {
            InsurancePolicy policy = invocation.getArgument(0);
            if (policy.getId() == null) {
                policy.setId(policyId);
            }
            return policy;
        });
        lenient().when(patientInsuranceRepository.save(any(PatientInsurance.class))).thenAnswer(invocation -> {
            PatientInsurance insurance = invocation.getArgument(0);
            if (insurance.getId() == null) {
                insurance.setId(patientInsuranceId);
            }
            return insurance;
        });
        lenient().when(insuranceClaimRepository.save(any(InsuranceClaim.class))).thenAnswer(invocation -> {
            InsuranceClaim claim = invocation.getArgument(0);
            if (claim.getId() == null) {
                claim.setId(claimId);
            }
            return claim;
        });
        lenient().when(claimDocumentRepository.save(any(ClaimDocument.class))).thenAnswer(invocation -> {
            ClaimDocument document = invocation.getArgument(0);
            if (document.getId() == null) {
                document.setId(documentId);
            }
            return document;
        });
    }

    @Test
    @DisplayName("INS-SRV-UT-001 - createPolicy should save a unique policy")
    void createPolicyShouldSaveUniquePolicy() {
        when(insurancePolicyRepository.existsByPolicyNumber("BHYT-001")).thenReturn(false);

        InsurancePolicyDTO result = policyService.createPolicy(policyRequest("BHYT-001", 80, "ACTIVE"));

        assertEquals("BHYT-001", result.getPolicyNumber());
        verify(insurancePolicyRepository).save(any(InsurancePolicy.class));
    }

    @Test
    @DisplayName("INS-SRV-UT-002 - createPolicy should reject duplicate policy number")
    void createPolicyShouldRejectDuplicatePolicyNumber() {
        when(insurancePolicyRepository.existsByPolicyNumber("BHYT-001")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> policyService.createPolicy(policyRequest("BHYT-001", 80, "ACTIVE")));
    }

    @Test
    @DisplayName("INS-SRV-UT-003 - createPolicy should reject coverage greater than 100 percent")
    void createPolicyShouldRejectCoverageGreaterThanOneHundred() {
        // Test Case ID: INS-SRV-UT-003 - Objective: insurance ratio should not exceed 100 percent.
        when(insurancePolicyRepository.existsByPolicyNumber("BHYT-OVER")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> policyService.createPolicy(policyRequest("BHYT-OVER", 150, "ACTIVE")));
    }

    @Test
    @DisplayName("INS-SRV-UT-004 - createPolicy should reject end date before start date")
    void createPolicyShouldRejectInvalidDateRange() {
        // Test Case ID: INS-SRV-UT-004 - Objective: policy date range must be chronologically valid.
        InsurancePolicyRequestDTO request = policyRequest("BHYT-DATE", 80, "ACTIVE");
        request.setStartDate(LocalDate.now().plusDays(10));
        request.setEndDate(LocalDate.now());
        when(insurancePolicyRepository.existsByPolicyNumber("BHYT-DATE")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> policyService.createPolicy(request));
    }

    @Test
    @DisplayName("INS-SRV-UT-005 - updatePolicy should update existing unique policy")
    void updatePolicyShouldUpdateExistingUniquePolicy() {
        InsurancePolicy existing = policy("BHYT-001", 80, "ACTIVE");
        when(insurancePolicyRepository.findById(policyId)).thenReturn(Optional.of(existing));
        when(insurancePolicyRepository.existsByPolicyNumber("BHYT-002")).thenReturn(false);

        InsurancePolicyDTO result = policyService.updatePolicy(policyId, policyRequest("BHYT-002", 90, "ACTIVE"));

        assertEquals("BHYT-002", result.getPolicyNumber());
    }

    @Test
    @DisplayName("INS-SRV-UT-006 - updatePolicy should reject duplicate changed policy number")
    void updatePolicyShouldRejectDuplicateChangedPolicyNumber() {
        InsurancePolicy existing = policy("BHYT-001", 80, "ACTIVE");
        when(insurancePolicyRepository.findById(policyId)).thenReturn(Optional.of(existing));
        when(insurancePolicyRepository.existsByPolicyNumber("BHYT-002")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> policyService.updatePolicy(policyId, policyRequest("BHYT-002", 80, "ACTIVE")));
    }

    @Test
    @DisplayName("INS-SRV-UT-007 - getPoliciesByStatus should return mapped policies")
    void getPoliciesByStatusShouldReturnMappedPolicies() {
        when(insurancePolicyRepository.findByStatus("ACTIVE")).thenReturn(List.of(policy("BHYT-001", 80, "ACTIVE")));

        List<InsurancePolicyDTO> result = policyService.getPoliciesByStatus("ACTIVE");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("INS-SRV-UT-008 - getPoliciesByCoverageAmount should query minimum coverage")
    void getPoliciesByCoverageAmountShouldQueryMinimumCoverage() {
        when(insurancePolicyRepository.findByCoverageAmountGreaterThanEqual(70)).thenReturn(List.of(policy("BHYT-001", 80, "ACTIVE")));

        policyService.getPoliciesByCoverageAmount(70);

        verify(insurancePolicyRepository).findByCoverageAmountGreaterThanEqual(70);
    }

    @Test
    @DisplayName("INS-SRV-UT-009 - deletePolicy should delete existing policy")
    void deletePolicyShouldDeleteExistingPolicy() {
        when(insurancePolicyRepository.existsById(policyId)).thenReturn(true);

        policyService.deletePolicy(policyId);

        verify(insurancePolicyRepository).deleteById(policyId);
    }

    @Test
    @DisplayName("INS-SRV-UT-010 - deletePolicy should throw when policy is missing")
    void deletePolicyShouldThrowWhenMissing() {
        when(insurancePolicyRepository.existsById(policyId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> policyService.deletePolicy(policyId));
    }

    @Test
    @DisplayName("INS-SRV-UT-011 - createPatientInsurance should save new patient insurance")
    void createPatientInsuranceShouldSaveNewPatientInsurance() {
        when(patientInsuranceRepository.existsByPatientId(patientId)).thenReturn(false);
        when(insurancePolicyRepository.findById(policyId)).thenReturn(Optional.of(policy("BHYT-001", 80, "ACTIVE")));

        PatientInsuranceDTO result = patientInsuranceService.createPatientInsurance(patientInsuranceRequest(patientId, "ACTIVE", LocalDate.now().plusDays(30)));

        assertEquals(patientId, result.getPatientId());
    }

    @Test
    @DisplayName("INS-SRV-UT-012 - createPatientInsurance should reject duplicate patient insurance")
    void createPatientInsuranceShouldRejectDuplicatePatientInsurance() {
        when(patientInsuranceRepository.existsByPatientId(patientId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> patientInsuranceService.createPatientInsurance(patientInsuranceRequest(patientId, "ACTIVE", LocalDate.now().plusDays(30))));
    }

    @Test
    @DisplayName("INS-SRV-UT-013 - createPatientInsurance should reject expired insurance")
    void createPatientInsuranceShouldRejectExpiredInsurance() {
        // Test Case ID: INS-SRV-UT-013 - Objective: active patient insurance should not be created already expired.
        when(patientInsuranceRepository.existsByPatientId(patientId)).thenReturn(false);
        when(insurancePolicyRepository.findById(policyId)).thenReturn(Optional.of(policy("BHYT-001", 80, "ACTIVE")));

        assertThrows(IllegalArgumentException.class,
                () -> patientInsuranceService.createPatientInsurance(patientInsuranceRequest(patientId, "ACTIVE", LocalDate.now().minusDays(1))));
    }

    @Test
    @DisplayName("INS-SRV-UT-014 - updatePatientInsurance should update same patient without duplicate check failure")
    void updatePatientInsuranceShouldUpdateSamePatient() {
        PatientInsurance existing = patientInsurance(patientId, "ACTIVE", LocalDate.now().plusDays(30), policy("BHYT-001", 80, "ACTIVE"));
        when(patientInsuranceRepository.findById(patientInsuranceId)).thenReturn(Optional.of(existing));
        when(insurancePolicyRepository.findById(policyId)).thenReturn(Optional.of(policy("BHYT-001", 80, "ACTIVE")));

        PatientInsuranceDTO result = patientInsuranceService.updatePatientInsurance(patientInsuranceId, patientInsuranceRequest(patientId, "INACTIVE", LocalDate.now().plusDays(30)));

        assertEquals("INACTIVE", result.getStatus());
    }

    @Test
    @DisplayName("INS-SRV-UT-015 - updatePatientInsurance should reject duplicate changed patient")
    void updatePatientInsuranceShouldRejectDuplicateChangedPatient() {
        UUID otherPatientId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        PatientInsurance existing = patientInsurance(patientId, "ACTIVE", LocalDate.now().plusDays(30), policy("BHYT-001", 80, "ACTIVE"));
        when(patientInsuranceRepository.findById(patientInsuranceId)).thenReturn(Optional.of(existing));
        when(patientInsuranceRepository.existsByPatientId(otherPatientId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> patientInsuranceService.updatePatientInsurance(patientInsuranceId, patientInsuranceRequest(otherPatientId, "ACTIVE", LocalDate.now().plusDays(30))));
    }

    @Test
    @DisplayName("INS-SRV-UT-016 - getActiveInsurances should query active insurance by today")
    void getActiveInsurancesShouldQueryToday() {
        when(patientInsuranceRepository.findActiveInsurances(any(LocalDate.class))).thenReturn(List.of(patientInsurance(patientId, "ACTIVE", LocalDate.now().plusDays(30), policy("BHYT-001", 80, "ACTIVE"))));

        List<PatientInsuranceDTO> result = patientInsuranceService.getActiveInsurances();

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("INS-SRV-UT-017 - getExpiredInsurances should query expired insurance by today")
    void getExpiredInsurancesShouldQueryToday() {
        when(patientInsuranceRepository.findExpiredInsurances(any(LocalDate.class))).thenReturn(List.of(patientInsurance(patientId, "EXPIRED", LocalDate.now().minusDays(1), policy("BHYT-001", 80, "ACTIVE"))));

        List<PatientInsuranceDTO> result = patientInsuranceService.getExpiredInsurances();

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("INS-SRV-UT-018 - isValidInsurance should return true for active non expired insurance")
    void isValidInsuranceShouldReturnTrueForActiveNonExpiredInsurance() {
        when(patientInsuranceRepository.findActiveInsuranceByPatientId(patientId)).thenReturn(Optional.of(patientInsurance(patientId, "ACTIVE", LocalDate.now().plusDays(30), policy("BHYT-001", 80, "ACTIVE"))));

        assertTrue(patientInsuranceService.isValidInsurance(patientId));
    }

    @Test
    @DisplayName("INS-SRV-UT-019 - isValidInsurance should return false when patient has no active insurance")
    void isValidInsuranceShouldReturnFalseWhenMissing() {
        when(patientInsuranceRepository.findActiveInsuranceByPatientId(patientId)).thenReturn(Optional.empty());

        assertFalse(patientInsuranceService.isValidInsurance(patientId));
    }

    @Test
    @DisplayName("INS-SRV-UT-020 - deletePatientInsurance should delete existing insurance")
    void deletePatientInsuranceShouldDeleteExistingInsurance() {
        when(patientInsuranceRepository.existsById(patientInsuranceId)).thenReturn(true);

        patientInsuranceService.deletePatientInsurance(patientInsuranceId);

        verify(patientInsuranceRepository).deleteById(patientInsuranceId);
    }

    @Test
    @DisplayName("INS-SRV-UT-021 - processInvoiceClaim should split covered item by coverage ratio and max")
    void processInvoiceClaimShouldSplitCoveredItem() {
        stubActivePatientInsurance(80);
        stubCoveredCatalogue(100_000);
        when(claimItemService.createClaimItem(any())).thenReturn(claimItemDto(claimItemId));

        InvoiceDTO result = claimService.processInvoiceClaim(invoiceRequest(invoiceItem(2, 100_000)));

        assertEquals(100_000, result.getInsuranceTotalPay());
        assertEquals(100_000, result.getPatientTotalPay());
    }

    @Test
    @DisplayName("INS-SRV-UT-022 - processInvoiceClaim should make patient pay full amount when service is uncovered")
    void processInvoiceClaimShouldMakePatientPayFullAmountWhenUncovered() {
        stubActivePatientInsurance(80);
        when(bhytCatalogueService.getBhytCatalogueByServiceCode(serviceReferenceId.toString())).thenThrow(new RuntimeException("not found"));

        InvoiceDTO result = claimService.processInvoiceClaim(invoiceRequest(invoiceItem(1, 100_000)));

        assertEquals(0, result.getInsuranceTotalPay());
        assertEquals(100_000, result.getPatientTotalPay());
        verify(claimItemService, never()).createClaimItem(any());
    }

    @Test
    @DisplayName("INS-SRV-UT-023 - processInvoiceClaim should reject expired patient insurance")
    void processInvoiceClaimShouldRejectExpiredPatientInsurance() {
        when(patientInsuranceRepository.findByPatientId(patientId)).thenReturn(Optional.of(patientInsurance(patientId, "ACTIVE", LocalDate.now().minusDays(1), policy("BHYT-001", 80, "ACTIVE"))));

        assertThrows(RuntimeException.class, () -> claimService.processInvoiceClaim(invoiceRequest(invoiceItem(1, 100_000))));
    }

    @Test
    @DisplayName("INS-SRV-UT-024 - processInvoiceClaim should reject inactive patient insurance")
    void processInvoiceClaimShouldRejectInactivePatientInsurance() {
        when(patientInsuranceRepository.findByPatientId(patientId)).thenReturn(Optional.of(patientInsurance(patientId, "INACTIVE", LocalDate.now().plusDays(30), policy("BHYT-001", 80, "ACTIVE"))));

        assertThrows(RuntimeException.class, () -> claimService.processInvoiceClaim(invoiceRequest(invoiceItem(1, 100_000))));
    }

    @Test
    @DisplayName("INS-SRV-UT-025 - processInvoiceClaim should not create claim when no item is covered")
    void processInvoiceClaimShouldNotCreateClaimWhenNoItemIsCovered() {
        // Test Case ID: INS-SRV-UT-025 - Objective: no claim record should be created when insurance pays zero.
        stubActivePatientInsurance(80);
        when(bhytCatalogueService.getBhytCatalogueByServiceCode(serviceReferenceId.toString())).thenThrow(new RuntimeException("not found"));

        claimService.processInvoiceClaim(invoiceRequest(invoiceItem(1, 100_000)));

        verify(insuranceClaimRepository, never()).save(any());
    }

    @Test
    @DisplayName("INS-SRV-UT-026 - processInvoiceItemsForValidation should assign claim item id for covered item")
    void processInvoiceItemsForValidationShouldAssignClaimItemIdForCoveredItem() {
        stubActivePatientInsurance(80);
        stubCoveredCatalogue(100_000);

        Set<InvoiceItemResponse> result = claimService.processInvoiceItemsForValidation(invoiceRequest(invoiceItem(1, 100_000)), patientId);

        assertNotNull(result.iterator().next().getClaimItemId());
    }

    @Test
    @DisplayName("INS-SRV-UT-027 - processInvoiceItemsForValidation should keep claim item id null for uncovered item")
    void processInvoiceItemsForValidationShouldKeepClaimItemIdNullForUncoveredItem() {
        stubActivePatientInsurance(80);
        when(bhytCatalogueService.getBhytCatalogueByServiceCode(serviceReferenceId.toString())).thenThrow(new RuntimeException("not found"));

        Set<InvoiceItemResponse> result = claimService.processInvoiceItemsForValidation(invoiceRequest(invoiceItem(1, 100_000)), patientId);

        assertNull(result.iterator().next().getClaimItemId());
    }

    @Test
    @DisplayName("INS-SRV-UT-028 - createClaimFromValidationEvent should aggregate totals")
    void createClaimFromValidationEventShouldAggregateTotals() {
        when(patientInsuranceRepository.findByPatientId(patientId)).thenReturn(Optional.of(patientInsurance(patientId, "ACTIVE", LocalDate.now().plusDays(30), policy("BHYT-001", 80, "ACTIVE"))));

        InsuranceClaimDTO result = claimService.createClaimFromValidationEvent(claimId, patientId, Set.of(invoiceItemResponse(60_000, 40_000)));

        assertEquals(100_000, result.getTotalClaimAmount());
    }

    @Test
    @DisplayName("INS-SRV-UT-029 - createClaimItemRequestFromInvoiceItem should calculate insurance pay ratio")
    void createClaimItemRequestFromInvoiceItemShouldCalculateRatio() {
        ClaimItemRequestDTO result = claimService.createClaimItemRequestFromInvoiceItem(invoiceItemResponse(60_000, 40_000), claimId);

        assertEquals(0.6f, result.getInsurancePayRatio(), 0.001f);
    }

    @Test
    @DisplayName("INS-SRV-UT-030 - createClaim should save claim for existing patient insurance")
    void createClaimShouldSaveClaimForExistingPatientInsurance() {
        when(patientInsuranceRepository.findById(patientInsuranceId)).thenReturn(Optional.of(patientInsurance(patientId, "ACTIVE", LocalDate.now().plusDays(30), policy("BHYT-001", 80, "ACTIVE"))));

        InsuranceClaimDTO result = claimService.createClaim(claimRequest("PENDING", patientInsuranceId));

        assertEquals("PENDING", result.getStatus());
    }

    @Test
    @DisplayName("INS-SRV-UT-031 - updateClaim should update existing claim")
    void updateClaimShouldUpdateExistingClaim() {
        when(insuranceClaimRepository.findById(claimId)).thenReturn(Optional.of(claim("PENDING")));
        when(patientInsuranceRepository.findById(patientInsuranceId)).thenReturn(Optional.of(patientInsurance(patientId, "ACTIVE", LocalDate.now().plusDays(30), policy("BHYT-001", 80, "ACTIVE"))));

        InsuranceClaimDTO result = claimService.updateClaim(claimId, claimRequest("REVIEWING", patientInsuranceId));

        assertEquals("REVIEWING", result.getStatus());
    }

    @Test
    @DisplayName("INS-SRV-UT-032 - approveClaim should set approved status and approval date")
    void approveClaimShouldSetApprovedStatusAndDate() {
        when(insuranceClaimRepository.findById(claimId)).thenReturn(Optional.of(claim("PENDING")));

        InsuranceClaimDTO result = claimService.approveClaim(claimId, 100_000);

        assertEquals("APPROVED", result.getStatus());
        assertNotNull(result.getApprovalDate());
    }

    @Test
    @DisplayName("INS-SRV-UT-033 - approveClaim should reject an already rejected claim")
    void approveClaimShouldRejectAlreadyRejectedClaim() {
        // Test Case ID: INS-SRV-UT-033 - Objective: rejected claim should not be approved later.
        when(insuranceClaimRepository.findById(claimId)).thenReturn(Optional.of(claim("REJECTED")));

        assertThrows(IllegalStateException.class, () -> claimService.approveClaim(claimId, 100_000));
    }

    @Test
    @DisplayName("INS-SRV-UT-034 - rejectClaim should set rejected status and reason")
    void rejectClaimShouldSetRejectedStatusAndReason() {
        when(insuranceClaimRepository.findById(claimId)).thenReturn(Optional.of(claim("PENDING")));

        InsuranceClaimDTO result = claimService.rejectClaim(claimId, "missing document");

        assertEquals("REJECTED", result.getStatus());
        assertTrue(result.getNotes().contains("missing document"));
    }

    @Test
    @DisplayName("INS-SRV-UT-035 - deleteClaim should throw when claim is missing")
    void deleteClaimShouldThrowWhenMissing() {
        when(insuranceClaimRepository.existsById(claimId)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> claimService.deleteClaim(claimId));
    }

    @Test
    @DisplayName("INS-SRV-UT-036 - createDocument should save document for existing claim")
    void createDocumentShouldSaveDocumentForExistingClaim() {
        when(insuranceClaimRepository.findById(claimId)).thenReturn(Optional.of(claim("PENDING")));

        ClaimDocumentDTO result = documentService.createDocument(documentRequest("PENDING"));

        assertEquals("PENDING", result.getStatus());
    }

    @Test
    @DisplayName("INS-SRV-UT-037 - getDocumentsByClaimId should return mapped documents")
    void getDocumentsByClaimIdShouldReturnMappedDocuments() {
        when(claimDocumentRepository.findByInsuranceClaimId(claimId)).thenReturn(List.of(document("PENDING")));

        List<ClaimDocumentDTO> result = documentService.getDocumentsByClaimId(claimId);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("INS-SRV-UT-038 - getDocumentsByStatus should query status")
    void getDocumentsByStatusShouldQueryStatus() {
        when(claimDocumentRepository.findByStatus("PENDING")).thenReturn(List.of(document("PENDING")));

        documentService.getDocumentsByStatus("PENDING");

        verify(claimDocumentRepository).findByStatus("PENDING");
    }

    @Test
    @DisplayName("INS-SRV-UT-039 - updateDocument should update existing document")
    void updateDocumentShouldUpdateExistingDocument() {
        when(claimDocumentRepository.findById(documentId)).thenReturn(Optional.of(document("PENDING")));
        when(insuranceClaimRepository.findById(claimId)).thenReturn(Optional.of(claim("PENDING")));

        ClaimDocumentDTO result = documentService.updateDocument(documentId, documentRequest("APPROVED"));

        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    @DisplayName("INS-SRV-UT-040 - updateDocumentStatus should change document status")
    void updateDocumentStatusShouldChangeDocumentStatus() {
        when(claimDocumentRepository.findById(documentId)).thenReturn(Optional.of(document("PENDING")));

        ClaimDocumentDTO result = documentService.updateDocumentStatus(documentId, "APPROVED");

        assertEquals("APPROVED", result.getStatus());
    }

    @Test
    @DisplayName("INS-SRV-UT-041 - updateDocumentStatus should reject invalid status")
    void updateDocumentStatusShouldRejectInvalidStatus() {
        // Test Case ID: INS-SRV-UT-041 - Objective: document workflow should reject unknown statuses.
        when(claimDocumentRepository.findById(documentId)).thenReturn(Optional.of(document("PENDING")));

        assertThrows(IllegalArgumentException.class, () -> documentService.updateDocumentStatus(documentId, "INVALID"));
    }

    @Test
    @DisplayName("INS-SRV-UT-042 - deleteDocument should delete existing document")
    void deleteDocumentShouldDeleteExistingDocument() {
        when(claimDocumentRepository.existsById(documentId)).thenReturn(true);

        documentService.deleteDocument(documentId);

        verify(claimDocumentRepository).deleteById(documentId);
    }

    @Test
    @DisplayName("INS-SRV-UT-043 - deleteDocumentsByClaimId should delete all claim documents")
    void deleteDocumentsByClaimIdShouldDeleteAllClaimDocuments() {
        documentService.deleteDocumentsByClaimId(claimId);

        verify(claimDocumentRepository).deleteByInsuranceClaimId(claimId);
    }

    private InsurancePolicyRequestDTO policyRequest(String policyNumber, int coverageAmount, String status) {
        return new InsurancePolicyRequestDTO(
                policyNumber,
                "BHYT",
                coverageAmount,
                10_000,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusYears(1),
                status
        );
    }

    private InsurancePolicy policy(String policyNumber, int coverageAmount, String status) {
        InsurancePolicy policy = new InsurancePolicy();
        policy.setId(policyId);
        policy.setPolicyNumber(policyNumber);
        policy.setPolicyType("BHYT");
        policy.setCoverageAmount(coverageAmount);
        policy.setDeductible(10_000);
        policy.setStartDate(LocalDate.now().minusDays(1));
        policy.setEndDate(LocalDate.now().plusYears(1));
        policy.setStatus(status);
        return policy;
    }

    private PatientInsuranceRequestDTO patientInsuranceRequest(UUID requestPatientId, String status, LocalDate expiryDate) {
        return new PatientInsuranceRequestDTO(
                requestPatientId,
                LocalDate.now().minusDays(1),
                expiryDate,
                status,
                policyId
        );
    }

    private PatientInsurance patientInsurance(UUID requestPatientId, String status, LocalDate expiryDate, InsurancePolicy policy) {
        PatientInsurance insurance = new PatientInsurance();
        insurance.setId(patientInsuranceId);
        insurance.setPatientId(requestPatientId);
        insurance.setIssueDate(LocalDate.now().minusDays(1));
        insurance.setExpiryDate(expiryDate);
        insurance.setStatus(status);
        insurance.setInsurancePolicy(policy);
        return insurance;
    }

    private InsuranceClaimRequestDTO claimRequest(String status, UUID requestPatientInsuranceId) {
        return new InsuranceClaimRequestDTO(status, 40_000, 100_000, 60_000, LocalDateTime.now(), null, "claim note", requestPatientInsuranceId);
    }

    private InsuranceClaim claim(String status) {
        InsuranceClaim claim = new InsuranceClaim();
        claim.setId(claimId);
        claim.setStatus(status);
        claim.setPatientInsurance(patientInsurance(patientId, "ACTIVE", LocalDate.now().plusDays(30), policy("BHYT-001", 80, "ACTIVE")));
        claim.setPatientPayAmount(40_000);
        claim.setTotalInsurancePay(60_000);
        claim.setTotalClaimAmount(100_000);
        claim.setClaimDate(LocalDateTime.now());
        return claim;
    }

    private ClaimDocumentRequestDTO documentRequest(String status) {
        return new ClaimDocumentRequestDTO("claims/doc.pdf", "MEDICAL_RECORD", status, LocalDateTime.now(), claimId);
    }

    private ClaimDocument document(String status) {
        ClaimDocument document = new ClaimDocument();
        document.setId(documentId);
        document.setFilePath("claims/doc.pdf");
        document.setDocumentType("MEDICAL_RECORD");
        document.setStatus(status);
        document.setUploadAt(LocalDateTime.now());
        document.setInsuranceClaim(claim("PENDING"));
        return document;
    }

    private InvoiceCheckerRequest invoiceRequest(InvoiceItemCheckerRequest item) {
        InvoiceCheckerRequest request = new InvoiceCheckerRequest();
        request.setId(UUID.randomUUID());
        request.setPatientId(patientId);
        request.setReceptionistId(UUID.randomUUID());
        request.setAppointmentId(UUID.randomUUID());
        request.setTotalAmount(item.getQuantity() * item.getUnitPrice());
        request.setCurrency("VND");
        request.setStatus("PENDING");
        request.setIssueAt(LocalDateTime.now());
        request.setItems(Set.of(item));
        return request;
    }

    private InvoiceItemCheckerRequest invoiceItem(int quantity, int unitPrice) {
        return new InvoiceItemCheckerRequest(UUID.randomUUID(), serviceReferenceId, "Dental", quantity, "Cao voi", unitPrice);
    }

    private InvoiceItemResponse invoiceItemResponse(int insurancePay, int patientPay) {
        return InvoiceItemResponse.builder()
                .id(UUID.randomUUID())
                .referenceId(serviceReferenceId)
                .serviceType("Dental")
                .quantity(1)
                .unitPrice(insurancePay + patientPay)
                .description("Cao voi")
                .insurancePayAmount(insurancePay)
                .patientPayAmount(patientPay)
                .claimItemId(claimItemId)
                .bhytCatalogueId(catalogueId)
                .build();
    }

    private void stubActivePatientInsurance(int coverageAmount) {
        when(patientInsuranceRepository.findByPatientId(patientId))
                .thenReturn(Optional.of(patientInsurance(patientId, "ACTIVE", LocalDate.now().plusDays(30), policy("BHYT-001", coverageAmount, "ACTIVE"))));
    }

    private void stubCoveredCatalogue(int maxCoverageAmount) {
        BhytCatalogueDTO catalogue = new BhytCatalogueDTO();
        catalogue.setId(catalogueId);
        catalogue.setServiceCode(serviceReferenceId.toString());
        catalogue.setServiceName("Cao voi");
        catalogue.setServiceType("Dental");
        catalogue.setIsCovered(true);
        catalogue.setMaxCoverageAmount(maxCoverageAmount);
        when(bhytCatalogueService.getBhytCatalogueByServiceCode(serviceReferenceId.toString())).thenReturn(catalogue);
    }

    private ClaimItemDTO claimItemDto(UUID id) {
        ClaimItemDTO dto = new ClaimItemDTO();
        dto.setId(id);
        return dto;
    }
}
