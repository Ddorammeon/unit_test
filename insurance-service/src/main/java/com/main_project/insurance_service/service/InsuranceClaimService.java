package com.main_project.insurance_service.service;

import com.main_project.insurance_service.dto.*;
import com.main_project.insurance_service.exceptions.AppException;
import com.main_project.insurance_service.exceptions.enums.ErrorCode;
import com.do_an.common.model.InvoiceCheckerRequest;
import com.do_an.common.model.InvoiceItemCheckerRequest;
import com.do_an.common.model.InvoiceItemResponse;
import com.main_project.insurance_service.entity.InsuranceClaim;
import com.main_project.insurance_service.entity.PatientInsurance;
import com.main_project.insurance_service.repository.InsuranceClaimRepository;
import com.main_project.insurance_service.repository.PatientInsuranceRepository;
import com.main_project.insurance_service.util.EntityDTOMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InsuranceClaimService implements IInsuranceClaimService {

    private final InsuranceClaimRepository insuranceClaimRepository;
    private final PatientInsuranceRepository patientInsuranceRepository;
    private final IBhytCatalogueService bhytCatalogueService;
    private final IClaimItemService claimItemService;
    private final IInsurancePolicyService insurancePolicyService;
    private final EntityDTOMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<InsuranceClaimDTO> getAllClaims() {
        return insuranceClaimRepository.findAll()
                .stream()
                .map(mapper::toInsuranceClaimDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InsuranceClaimDTO> getClaimById(UUID id) {
        return insuranceClaimRepository.findById(id)
                .map(mapper::toInsuranceClaimDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsuranceClaimDTO> getClaimsByStatus(String status) {
        return insuranceClaimRepository.findByStatus(status)
                .stream()
                .map(mapper::toInsuranceClaimDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsuranceClaimDTO> getClaimsByPatientInsuranceId(UUID patientInsuranceId) {
        return insuranceClaimRepository.findByPatientInsuranceId(patientInsuranceId)
                .stream()
                .map(mapper::toInsuranceClaimDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsuranceClaimDTO> getClaimsByPatientId(UUID patientId) {
        return insuranceClaimRepository.findByPatientId(patientId)
                .stream()
                .map(mapper::toInsuranceClaimDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsuranceClaimDTO> getClaimsByDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        return insuranceClaimRepository.findByClaimDateBetween(startDate, endDate)
                .stream()
                .map(mapper::toInsuranceClaimDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsuranceClaimDTO> getClaimsByStatusAndMinAmount(String status, Integer minAmount) {
        return insuranceClaimRepository.findByStatusAndTotalClaimAmountGreaterThanEqual(status, minAmount)
                .stream()
                .map(mapper::toInsuranceClaimDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getClaimCountByPatientInsurance(UUID patientInsuranceId) {
        return insuranceClaimRepository.countByPatientInsuranceId(patientInsuranceId);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalApprovedAmountByPatientInsurance(UUID patientInsuranceId) {
        Integer total = insuranceClaimRepository.getTotalApprovedAmountByPatientInsurance(patientInsuranceId);
        return total != null ? total : 0;
    }

    public InvoiceDTO processInvoiceClaim(InvoiceCheckerRequest requestDTO) {
        PatientInsurance patientInsurance = patientInsuranceRepository.findByPatientId(requestDTO.getPatientId())
                .orElseThrow(() -> new AppException(ErrorCode.PATIENT_NOT_EXISTED));

        LocalDate currentDate = LocalDate.now();
        if (patientInsurance.getExpiryDate().isBefore(currentDate)) {
            throw new RuntimeException("BHYT đã hết hạn sử dụng. Ngày hết hạn: " + patientInsurance.getExpiryDate());
        }

        if (!"ACTIVE".equals(patientInsurance.getStatus())) {
            throw new RuntimeException("BHYT không trong trạng thái hoạt động");
        }

        Float bhytPayRatio = patientInsurance.getInsurancePolicy().getCoverageAmount() / 100.0f;

        Set<InvoiceItemDTO> processedItems = new HashSet<>();
        List<ProcessedInvoiceItem> itemsForClaim = new ArrayList<>();
        Integer totalInsurancePay = 0;
        Integer totalPatientPay = 0;

        for (InvoiceItemCheckerRequest itemRequest : requestDTO.getItems()) {
            ProcessedInvoiceItem processedItem = processInvoiceItem(itemRequest, bhytPayRatio);
            processedItems.add(processedItem.getInvoiceItem());

            if (processedItem.getInvoiceItem().getInsurancePayAmount() > 0) {
                itemsForClaim.add(processedItem);
            }

            totalInsurancePay += processedItem.getInvoiceItem().getInsurancePayAmount();
            totalPatientPay += processedItem.getInvoiceItem().getPatientPayAmount();
        }

        InsuranceClaim insuranceClaim = new InsuranceClaim();
        insuranceClaim.setPatientInsurance(patientInsurance);
        insuranceClaim.setStatus("PENDING");
        insuranceClaim.setTotalClaimAmount(requestDTO.getTotalAmount());
        insuranceClaim.setTotalInsurancePay(totalInsurancePay);
        insuranceClaim.setPatientPayAmount(totalPatientPay);
        insuranceClaim.setClaimDate(LocalDateTime.now());

        //insuranceClaim.setNotes("Claim created from invoice checker request");

        InsuranceClaim savedClaim = insuranceClaimRepository.save(insuranceClaim);

        for (ProcessedInvoiceItem processedItem : itemsForClaim) {
            ClaimItemRequestDTO claimItemRequest = createClaimItemRequestFromProcessedItem(processedItem, savedClaim.getId());
            ClaimItemDTO createdClaimItem = claimItemService.createClaimItem(claimItemRequest);

            for (InvoiceItemDTO invoiceItem : processedItems) {
                if (invoiceItem.getId().equals(processedItem.getInvoiceItem().getId())) {
                    invoiceItem.setClaimItemId(createdClaimItem.getId());
                    break;
                }
            }
        }

        InvoiceDTO invoiceDTO = new InvoiceDTO();
        invoiceDTO.setId(requestDTO.getId());
        invoiceDTO.setReceptionistId(requestDTO.getReceptionistId());
        invoiceDTO.setAppointmentId(requestDTO.getAppointmentId());
        invoiceDTO.setInsuranceClaimId(savedClaim.getId());
        invoiceDTO.setTotalAmount(requestDTO.getTotalAmount());
        invoiceDTO.setCurrency(requestDTO.getCurrency());
        invoiceDTO.setStatus(requestDTO.getStatus());
        invoiceDTO.setInsuranceTotalPay(totalInsurancePay);
        invoiceDTO.setPatientTotalPay(totalPatientPay);
        invoiceDTO.setIssueAt(requestDTO.getIssueAt());
        invoiceDTO.setPaidAt(requestDTO.getPaidAt());
        invoiceDTO.setUpdateAt(requestDTO.getUpdateAt());
        invoiceDTO.setItems(processedItems);

        return invoiceDTO;
    }

    ProcessedInvoiceItem processInvoiceItem(InvoiceItemCheckerRequest itemRequest, Float bhytPayRatio) {
        InvoiceItemDTO itemDTO = new InvoiceItemDTO();
        itemDTO.setId(itemRequest.getId());
        itemDTO.setReferenceId(itemRequest.getReferenceId());
        itemDTO.setServiceType(itemRequest.getServiceType());
        itemDTO.setQuantity(itemRequest.getQuantity());
        itemDTO.setDescription(itemRequest.getDescription());
        itemDTO.setUnitPrice(itemRequest.getUnitPrice());

        Integer totalAmount = itemRequest.getQuantity() * itemRequest.getUnitPrice();
        Integer insurancePayAmount = 0;
        Integer patientPayAmount = totalAmount;
        UUID bhytCatalogueId = null;

        try {
            BhytCatalogueDTO bhytService = bhytCatalogueService.getBhytCatalogueByServiceCode(itemRequest.getReferenceId().toString());

            if (bhytService.getIsCovered() && bhytService.getMaxCoverageAmount() > 0) {
                Integer maxCoverage = bhytService.getMaxCoverageAmount();
                insurancePayAmount = Math.round(Math.min(maxCoverage, totalAmount * bhytPayRatio));
                patientPayAmount = totalAmount - insurancePayAmount;
                bhytCatalogueId = bhytService.getId();
            }
        } catch (RuntimeException e) {
            System.out.println("Service not found in BHYT catalogue: " + itemRequest.getReferenceId() + ". Patient pays full amount.");
        }

        itemDTO.setInsurancePayAmount(insurancePayAmount);
        itemDTO.setPatientPayAmount(patientPayAmount);

        return new ProcessedInvoiceItem(itemDTO, bhytCatalogueId);
    }

    private ClaimItemRequestDTO createClaimItemRequestFromProcessedItem(ProcessedInvoiceItem processedItem, UUID claimId) {
        InvoiceItemDTO invoiceItem = processedItem.getInvoiceItem();
        Integer totalAmount = invoiceItem.getQuantity() * invoiceItem.getUnitPrice();

        ClaimItemRequestDTO request = new ClaimItemRequestDTO();
        request.setQuantity(invoiceItem.getQuantity());
        request.setUnitPrice(invoiceItem.getUnitPrice());
        request.setTotalAmount(totalAmount);

        Float ratio = totalAmount > 0 ? (float) invoiceItem.getInsurancePayAmount() / totalAmount : 0f;
        request.setInsurancePayRatio(ratio);
        request.setInsurancePayAmount(invoiceItem.getInsurancePayAmount());
        request.setPatientPayAmount(invoiceItem.getPatientPayAmount());
        request.setInsuranceClaimId(claimId);
        request.setBhytCatalogueId(processedItem.getBhytCatalogueId());

        return request;
    }


    @Override
    public InsuranceClaimDTO createClaim(InsuranceClaimRequestDTO requestDTO) {
        PatientInsurance patientInsurance = patientInsuranceRepository.findById(requestDTO.getPatientInsuranceId())
                .orElseThrow(() -> new RuntimeException("Patient insurance not found with id: " + requestDTO.getPatientInsuranceId()));

        InsuranceClaim entity = mapper.toInsuranceClaimEntity(requestDTO, patientInsurance);
        InsuranceClaim savedEntity = insuranceClaimRepository.save(entity);
        return mapper.toInsuranceClaimDTO(savedEntity);
    }

    @Override
    public InsuranceClaimDTO updateClaim(UUID id, InsuranceClaimRequestDTO requestDTO) {
        InsuranceClaim existingEntity = insuranceClaimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insurance claim not found with id: " + id));

        PatientInsurance patientInsurance = patientInsuranceRepository.findById(requestDTO.getPatientInsuranceId())
                .orElseThrow(() -> new RuntimeException("Patient insurance not found with id: " + requestDTO.getPatientInsuranceId()));

        mapper.updateInsuranceClaimEntity(existingEntity, requestDTO, patientInsurance);
        InsuranceClaim updatedEntity = insuranceClaimRepository.save(existingEntity);
        return mapper.toInsuranceClaimDTO(updatedEntity);
    }

    @Override
    public InsuranceClaimDTO approveClaim(UUID id, Integer approvedAmount) {
        InsuranceClaim existingEntity = insuranceClaimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insurance claim not found with id: " + id));

        existingEntity.setStatus("APPROVED");
        existingEntity.setApprovalDate(LocalDateTime.now());
        // Note: approvedAmount parameter is ignored as approvedAmount field was removed

        InsuranceClaim updatedEntity = insuranceClaimRepository.save(existingEntity);
        return mapper.toInsuranceClaimDTO(updatedEntity);
    }

    @Override
    public InsuranceClaimDTO rejectClaim(UUID id, String reason) {
        InsuranceClaim existingEntity = insuranceClaimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu bảo hiểm với ID: " + id));

        existingEntity.setStatus("REJECTED");
        existingEntity.setApprovalDate(LocalDateTime.now());
        //existingEntity.setNotes(existingEntity.getNotes() + (existingEntity.getNotes() != null ? " | " : "") + "Lí do từ chối: " + reason);
        existingEntity.setNotes( "Lí do từ chối: " + reason);
        InsuranceClaim updatedEntity = insuranceClaimRepository.save(existingEntity);
        return mapper.toInsuranceClaimDTO(updatedEntity);
    }

    @Override
    public void deleteClaim(UUID id) {
        if (!insuranceClaimRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy yêu cầu bảo hiểm với ID: " + id);
        }
        insuranceClaimRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<InvoiceItemResponse> processInvoiceItemsForValidation(InvoiceCheckerRequest request, UUID patientId) {
        PatientInsurance patientInsurance = patientInsuranceRepository.findByPatientId(patientId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bảo hiểm của bệnh nhân: " + patientId));

        if (patientInsurance.getInsurancePolicy() == null) {
            throw new RuntimeException("Không tìm thấy chính sách bảo hiểm nào");
        }

        Float bhytPayRatio = patientInsurance.getInsurancePolicy().getCoverageAmount() / 100.0f;
        Set<InvoiceItemResponse> itemResponses = new HashSet<>();

        for (InvoiceItemCheckerRequest itemRequest : request.getItems()) {
            ProcessedInvoiceItem processedItem = processInvoiceItem(itemRequest, bhytPayRatio);
            InvoiceItemDTO dto = processedItem.getInvoiceItem();

            UUID claimItemId = (dto.getInsurancePayAmount() > 0) ? UUID.randomUUID() : null;
            InvoiceItemResponse response = InvoiceItemResponse.builder()
                    .id(dto.getId())
                    .referenceId(dto.getReferenceId())
                    .serviceType(dto.getServiceType())
                    .quantity(dto.getQuantity())
                    .description(dto.getDescription())
                    .unitPrice(dto.getUnitPrice())
                    .insurancePayAmount(dto.getInsurancePayAmount())
                    .patientPayAmount(dto.getPatientPayAmount())
                    .claimItemId(claimItemId)
                    .bhytCatalogueId(processedItem.getBhytCatalogueId())
                    .build();

            itemResponses.add(response);
        }

        return itemResponses;
    }

    @Override
    @Transactional
    public InsuranceClaimDTO createClaimFromValidationEvent(UUID claimId, UUID patientId, Set<InvoiceItemResponse> items) {
        PatientInsurance patientInsurance = patientInsuranceRepository.findByPatientId(patientId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bảo hiểm của bệnh nhân: " + patientId));

        int totalInsurancePay = 0;
        int totalPatientPay = 0;
        int totalClaimAmount = 0;

        for (InvoiceItemResponse item : items) {
            totalInsurancePay += item.getInsurancePayAmount();
            totalPatientPay += item.getPatientPayAmount();
            totalClaimAmount += (item.getInsurancePayAmount() + item.getPatientPayAmount());
        }

        InsuranceClaim insuranceClaim = new InsuranceClaim();
        insuranceClaim.setId(claimId);
        insuranceClaim.setPatientInsurance(patientInsurance);
        insuranceClaim.setStatus("PENDING");
        insuranceClaim.setTotalClaimAmount(totalClaimAmount);
        insuranceClaim.setTotalInsurancePay(totalInsurancePay);
        insuranceClaim.setPatientPayAmount(totalPatientPay);
        insuranceClaim.setClaimDate(LocalDateTime.now());

        InsuranceClaim savedClaim = insuranceClaimRepository.save(insuranceClaim);
        return mapper.toInsuranceClaimDTO(savedClaim);
    }

    @Override
    public ClaimItemRequestDTO createClaimItemRequestFromInvoiceItem(InvoiceItemResponse item, UUID claimId) {
        ClaimItemRequestDTO request = new ClaimItemRequestDTO();

        if (item.getClaimItemId() != null) {
            request.setId(item.getClaimItemId());
        }
        request.setInsuranceClaimId(claimId);
        request.setQuantity(item.getQuantity());
        request.setUnitPrice(item.getUnitPrice());
        request.setTotalAmount(item.getQuantity() * item.getUnitPrice());
        request.setInsurancePayAmount(item.getInsurancePayAmount());
        request.setPatientPayAmount(item.getPatientPayAmount());

        Float ratio = item.getQuantity() * item.getUnitPrice() > 0 
                ? (float) item.getInsurancePayAmount() / (item.getQuantity() * item.getUnitPrice()) 
                : 0f;
        request.setInsurancePayRatio(ratio);
        request.setBhytCatalogueId(item.getBhytCatalogueId());

        return request;
    }
}





