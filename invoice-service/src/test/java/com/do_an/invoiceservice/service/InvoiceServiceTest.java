package com.do_an.invoiceservice.service;

import com.do_an.common.model.InvoiceItemCheckerRequest;
import com.do_an.common.model.InvoiceItemResponse;
import com.do_an.common.model.MedicineItem;
import com.do_an.invoiceservice.client.AppointmentClient;
import com.do_an.invoiceservice.dto.request.AddLabTestChargeRequestDTO;
import com.do_an.invoiceservice.dto.request.CreateInvoiceItemRequestDTO;
import com.do_an.invoiceservice.dto.request.CreateInvoiceRequestDTO;
import com.do_an.invoiceservice.dto.response.AppointmentDTO;
import com.do_an.invoiceservice.dto.response.InvoiceItemResponseDTO;
import com.do_an.invoiceservice.dto.response.InvoiceResponseDTO;
import com.do_an.invoiceservice.entity.Invoice;
import com.do_an.invoiceservice.entity.InvoiceItem;
import com.do_an.invoiceservice.exception.InvoiceNotFoundException;
import com.do_an.invoiceservice.mapper.InvoiceItemMapper;
import com.do_an.invoiceservice.mapper.InvoiceMapper;
import com.do_an.invoiceservice.repository.InvoiceItemRepository;
import com.do_an.invoiceservice.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private InvoiceItemRepository invoiceItemRepository;
    @Mock
    private InvoiceMapper invoiceMapper;
    @Mock
    private InvoiceItemMapper invoiceItemMapper;
    @Mock
    private AppointmentClient appointmentClient;

    private InvoiceService invoiceService;

    private final UUID invoiceId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID appointmentId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID receptionistId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private final UUID patientId = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private final UUID insuranceClaimId = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private final UUID labTestId = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private final UUID existingItemId = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private final UUID removedItemId = UUID.fromString("88888888-8888-8888-8888-888888888888");

    @BeforeEach
    void setUp() {
        invoiceService = new InvoiceService(
                invoiceRepository,
                invoiceItemRepository,
                invoiceMapper,
                invoiceItemMapper,
                appointmentClient
        );

        lenient().when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(invoiceItemRepository.save(any(InvoiceItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(invoiceItemRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().doNothing().when(invoiceItemRepository).deleteAll(anyList());
        lenient().doNothing().when(invoiceItemRepository).deleteByInvoice_IdAndServiceType(any(), anyString());
        lenient().when(invoiceRepository.findAll()).thenReturn(List.of());
        lenient().when(invoiceMapper.toResponseDto(any(Invoice.class))).thenAnswer(invocation -> toResponseDto(invocation.getArgument(0)));
        lenient().when(invoiceMapper.toResponseDtoList(anyList())).thenAnswer(invocation -> {
            List<Invoice> invoices = invocation.getArgument(0);
            return invoices.stream().map(this::toResponseDto).collect(Collectors.toList());
        });
        lenient().when(invoiceMapper.toEntity(any(CreateInvoiceRequestDTO.class))).thenAnswer(invocation -> toInvoice(invocation.getArgument(0)));
        lenient().when(invoiceItemMapper.toEntity(any(CreateInvoiceItemRequestDTO.class))).thenAnswer(invocation -> toInvoiceItem(invocation.getArgument(0)));
        lenient().doAnswer(invocation -> {
            CreateInvoiceItemRequestDTO dto = invocation.getArgument(0);
            InvoiceItem entity = invocation.getArgument(1);
            entity.setReferenceId(dto.getReferenceId());
            entity.setServiceType(dto.getServiceType());
            entity.setQuantity(dto.getQuantity());
            entity.setDescription(dto.getDescription());
            entity.setUnitPrice(dto.getUnitPrice());
            entity.setClaimItemId(dto.getClaimItemId());
            return null;
        }).when(invoiceItemMapper).updateFromDto(any(CreateInvoiceItemRequestDTO.class), any(InvoiceItem.class));
        lenient().doAnswer(invocation -> {
            InvoiceItemResponse response = invocation.getArgument(0);
            InvoiceItem entity = invocation.getArgument(1);
            entity.setReferenceId(response.getReferenceId());
            entity.setServiceType(response.getServiceType());
            entity.setQuantity(response.getQuantity());
            entity.setDescription(response.getDescription());
            entity.setUnitPrice(response.getUnitPrice());
            entity.setInsurancePayAmount(response.getInsurancePayAmount());
            entity.setPatientPayAmount(response.getPatientPayAmount());
            entity.setClaimItemId(response.getClaimItemId());
            return null;
        }).when(invoiceItemMapper).updateFromResponse(any(InvoiceItemResponse.class), any(InvoiceItem.class));
    }

    @Test
    @DisplayName("INV-SRV-UT-001 - createInvoice should generate invoice id for new invoice")
    void createInvoiceShouldGenerateInvoiceId() {
        // Test Case ID: INV-SRV-UT-001 - Objective: new invoices should not keep a null primary key.
        CreateInvoiceRequestDTO request = createRequest(null, defaultCreateItems());

        InvoiceResponseDTO result = invoiceService.createInvoice(request);

        assertNotNull(result.getId());
    }

    @Test
    @DisplayName("INV-SRV-UT-002 - createInvoice should assign item ids")
    void createInvoiceShouldAssignItemIds() {
        // Test Case ID: INV-SRV-UT-002 - Objective: newly created invoice items should have stable ids.
        CreateInvoiceRequestDTO request = createRequest(invoiceId, defaultCreateItems());

        InvoiceResponseDTO result = invoiceService.createInvoice(request);

        assertTrue(result.getItems().stream().allMatch(item -> item.getId() != null));
    }

    @Test
    @DisplayName("INV-SRV-UT-003 - createInvoice should set pending status and issueAt")
    void createInvoiceShouldSetPendingStatusAndIssueAt() {
        CreateInvoiceRequestDTO request = createRequest(invoiceId, defaultCreateItems());

        InvoiceResponseDTO result = invoiceService.createInvoice(request);

        assertAll(
                () -> assertEquals("PENDING", result.getStatus()),
                () -> assertNotNull(result.getIssueAt())
        );
    }

    @Test
    @DisplayName("INV-SRV-UT-004 - createInvoice should calculate totals from items")
    void createInvoiceShouldCalculateTotalsFromItems() {
        CreateInvoiceRequestDTO request = createRequest(invoiceId, List.of(itemDto(null, "Dental", "Cạo vôi", 2, 150_000)));

        InvoiceResponseDTO result = invoiceService.createInvoice(request);

        assertEquals(300_000, result.getTotalAmount());
    }

    @Test
    @DisplayName("INV-SRV-UT-005 - createInvoice should set patient and insurance totals")
    void createInvoiceShouldSetFinancialTotals() {
        CreateInvoiceRequestDTO request = createRequest(invoiceId, defaultCreateItems());

        InvoiceResponseDTO result = invoiceService.createInvoice(request);

        assertAll(
                () -> assertEquals(300_000, result.getPatientTotalPay()),
                () -> assertEquals(0, result.getInsuranceTotalPay())
        );
    }

    @Test
    @DisplayName("INV-SRV-UT-006 - updateInvoice should recalculate total and save")
    void updateInvoiceShouldRecalculateTotalAndSave() {
        Invoice invoice = invoice("PENDING", 200_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 200_000));
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice));
        CreateInvoiceRequestDTO request = updateRequest(defaultUpdateItems());

        InvoiceResponseDTO result = invoiceService.updateInvoice(invoiceId, request);

        assertEquals(350_000, result.getTotalAmount());
        verify(invoiceRepository).save(invoice);
    }

    @Test
    @DisplayName("INV-SRV-UT-007 - updateInvoice should sync header fields")
    void updateInvoiceShouldSyncHeaderFields() {
        Invoice invoice = invoice("PENDING", 200_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 200_000));
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice));
        CreateInvoiceRequestDTO request = updateRequest(defaultUpdateItems());
        UUID newReceptionistId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        request.setReceptionistId(newReceptionistId);

        invoiceService.updateInvoice(invoiceId, request);

        assertEquals(newReceptionistId, invoice.getReceptionistId());
    }

    @Test
    @DisplayName("INV-SRV-UT-008 - updateInvoice should delete removed items")
    void updateInvoiceShouldDeleteRemovedItems() {
        Invoice existing = invoice("PENDING", 200_000,
                itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 200_000),
                itemEntity(removedItemId, "Dental", "Nhổ răng", 1, 100_000));
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(existing));
        when(invoiceItemRepository.findByInvoiceId(invoiceId)).thenReturn(new ArrayList<>(existing.getItems()));
        CreateInvoiceRequestDTO request = updateRequest(List.of(itemDto(existingItemId, "Dental", "Cạo vôi", 1, 200_000)));

        invoiceService.updateInvoice(invoiceId, request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<InvoiceItem>> captor = (ArgumentCaptor<List<InvoiceItem>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
        verify(invoiceItemRepository).deleteAll(captor.capture());
        assertTrue(captor.getValue().stream().anyMatch(item -> removedItemId.equals(item.getId())));
    }

    @Test
    @DisplayName("INV-SRV-UT-009 - updateInvoice should add new items")
    void updateInvoiceShouldAddNewItems() {
        Invoice existing = invoice("PENDING", 200_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 200_000));
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(existing));
        when(invoiceItemRepository.findByInvoiceId(invoiceId)).thenReturn(List.of(itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 200_000)));
        CreateInvoiceRequestDTO request = updateRequest(List.of(
                itemDto(existingItemId, "Dental", "Cạo vôi", 1, 200_000),
                itemDto(null, "Dental", "Chụp X-quang", 1, 150_000)
        ));

        InvoiceResponseDTO result = invoiceService.updateInvoice(invoiceId, request);

        assertEquals(350_000, result.getTotalAmount());
    }

    @Test
    @DisplayName("INV-SRV-UT-010 - updateInvoice should update existing items")
    void updateInvoiceShouldUpdateExistingItems() {
        Invoice existing = invoice("PENDING", 200_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 200_000));
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(existing));
        when(invoiceItemRepository.findByInvoiceId(invoiceId)).thenReturn(List.of(existing.getItems().iterator().next()));
        CreateInvoiceRequestDTO request = updateRequest(List.of(itemDto(existingItemId, "Dental", "Cạo vôi", 2, 200_000)));

        invoiceService.updateInvoice(invoiceId, request);

        InvoiceItem updated = existing.getItems().iterator().next();
        assertEquals(2, updated.getQuantity());
    }

    @Test
    @DisplayName("INV-SRV-UT-011 - markAsPaid should transition pending invoice to paid")
    void markAsPaidShouldTransitionPendingInvoice() {
        Invoice invoice = invoice("PENDING", 300_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 300_000));
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice));

        InvoiceResponseDTO result = invoiceService.markAsPaid(invoiceId);

        assertEquals("PAID", result.getStatus());
    }

    @Test
    @DisplayName("INV-SRV-UT-012 - markAsPaid should set paidAt")
    void markAsPaidShouldSetPaidAt() {
        Invoice invoice = invoice("PENDING", 300_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 300_000));
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice));

        InvoiceResponseDTO result = invoiceService.markAsPaid(invoiceId);

        assertNotNull(result.getPaidAt());
    }

    @Test
    @DisplayName("INV-SRV-UT-013 - cancelInvoice should cancel pending invoice")
    void cancelInvoiceShouldCancelPendingInvoice() {
        Invoice invoice = invoice("PENDING", 300_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 300_000));
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice));

        InvoiceResponseDTO result = invoiceService.cancelInvoice(invoiceId);

        assertEquals("CANCELLED", result.getStatus());
    }

    @Test
    @DisplayName("INV-SRV-UT-014 - cancelInvoice should reject paid invoice")
    void cancelInvoiceShouldRejectPaidInvoice() {
        Invoice invoice = invoice("PAID", 300_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 300_000));
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice));

        assertThrows(IllegalStateException.class, () -> invoiceService.cancelInvoice(invoiceId));
    }

    @Test
    @DisplayName("INV-SRV-UT-015 - getInvoiceById should return mapped invoice")
    void getInvoiceByIdShouldReturnMappedInvoice() {
        Invoice invoice = invoice("PENDING", 300_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 300_000));
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice));

        InvoiceResponseDTO result = invoiceService.getInvoiceById(invoiceId);

        assertEquals(invoiceId, result.getId());
    }

    @Test
    @DisplayName("INV-SRV-UT-016 - getInvoiceById should throw when invoice is missing")
    void getInvoiceByIdShouldThrowWhenMissing() {
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.empty());

        assertThrows(InvoiceNotFoundException.class, () -> invoiceService.getInvoiceById(invoiceId));
    }

    @Test
    @DisplayName("INV-SRV-UT-017 - listInvoices should use newest-first order when no status is provided")
    void listInvoicesShouldUseNewestFirstOrderWhenNoStatusProvided() {
        when(invoiceRepository.findAll()).thenReturn(List.of(invoice("PENDING", 300_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 300_000))));

        invoiceService.listInvoices(null);

        verify(invoiceRepository).findAllByOrderByIssueAtDesc();
    }

    @Test
    @DisplayName("INV-SRV-UT-018 - listInvoices should use status order query when status is provided")
    void listInvoicesShouldUseStatusOrderQueryWhenStatusProvided() {
        when(invoiceRepository.findAllByStatus("PENDING")).thenReturn(List.of(invoice("PENDING", 300_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 300_000))));

        invoiceService.listInvoices("PENDING");

        verify(invoiceRepository).findByStatusOrderByIssueAtDesc("PENDING");
    }

    @Test
    @DisplayName("INV-SRV-UT-019 - getInvoicesByAppointmentId should map invoices")
    void getInvoicesByAppointmentIdShouldMapInvoices() {
        when(invoiceRepository.findAllByAppointmentId(appointmentId)).thenReturn(List.of(
                invoice("PENDING", 300_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 300_000))
        ));

        List<InvoiceResponseDTO> result = invoiceService.getInvoicesByAppointmentId(appointmentId);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("INV-SRV-UT-020 - getInvoicesByAppointmentId should return empty list when repository is empty")
    void getInvoicesByAppointmentIdShouldReturnEmptyListWhenRepositoryIsEmpty() {
        when(invoiceRepository.findAllByAppointmentId(appointmentId)).thenReturn(List.of());

        List<InvoiceResponseDTO> result = invoiceService.getInvoicesByAppointmentId(appointmentId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("INV-SRV-UT-021 - getInvoicesByPatientId should return invoices for patient appointments")
    void getInvoicesByPatientIdShouldReturnInvoicesForAppointments() {
        when(appointmentClient.getAppointmentsByPatientId(patientId)).thenReturn(List.of(appointment(appointmentId)));
        when(invoiceRepository.findAllByAppointmentIdInOrderByIssueAtDesc(List.of(appointmentId))).thenReturn(List.of(
                invoice("PENDING", 300_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 300_000))
        ));

        List<InvoiceResponseDTO> result = invoiceService.getInvoicesByPatientId(patientId, null);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("INV-SRV-UT-022 - getInvoicesByPatientId should apply status filter")
    void getInvoicesByPatientIdShouldApplyStatusFilter() {
        when(appointmentClient.getAppointmentsByPatientId(patientId)).thenReturn(List.of(appointment(appointmentId)));
        when(invoiceRepository.findAllByAppointmentIdInAndStatusOrderByIssueAtDesc(List.of(appointmentId), "PAID")).thenReturn(List.of(
                invoice("PAID", 300_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 300_000))
        ));

        List<InvoiceResponseDTO> result = invoiceService.getInvoicesByPatientId(patientId, "PAID");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("INV-SRV-UT-023 - getInvoicesByPatientId should return empty on appointment client failure")
    void getInvoicesByPatientIdShouldReturnEmptyOnAppointmentClientFailure() {
        when(appointmentClient.getAppointmentsByPatientId(patientId)).thenThrow(new RuntimeException("appointment down"));

        assertTrue(invoiceService.getInvoicesByPatientId(patientId, null).isEmpty());
    }

    @Test
    @DisplayName("INV-SRV-UT-024 - addMedicineCharges should add items and update totals")
    void addMedicineChargesShouldAddItemsAndUpdateTotals() {
        Invoice invoice = invoice("PENDING", 200_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 200_000));
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice));
        Set<InvoiceItemCheckerRequest> items = Set.of(new InvoiceItemCheckerRequest(UUID.randomUUID(), UUID.randomUUID(), "MEDICINE", 2, "Amoxicillin", 50_000));
        List<MedicineItem> medicineItems = List.of(new MedicineItem(UUID.randomUUID(), items.iterator().next().getReferenceId(), "Amoxicillin", 2, 50_000, null, null, null, null, null));

        InvoiceResponseDTO result = invoiceService.addMedicineCharges(invoiceId, items, medicineItems);

        assertEquals(300_000, result.getTotalAmount());
    }

    @Test
    @DisplayName("INV-SRV-UT-025 - addMedicineCharges should use medicine name from matching medicine items")
    void addMedicineChargesShouldUseMedicineNameFromMatchingMedicineItems() {
        Invoice invoice = invoice("PENDING", 200_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 200_000));
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice));
        UUID referenceId = UUID.randomUUID();
        Set<InvoiceItemCheckerRequest> items = Set.of(new InvoiceItemCheckerRequest(UUID.randomUUID(), referenceId, "MEDICINE", 1, "Old name", 50_000));
        List<MedicineItem> medicineItems = List.of(new MedicineItem(UUID.randomUUID(), referenceId, "Paracetamol", 1, 50_000, null, null, null, null, null));

        InvoiceResponseDTO result = invoiceService.addMedicineCharges(invoiceId, items, medicineItems);

        assertTrue(result.getItems().stream().anyMatch(item -> "Paracetamol".equals(item.getDescription())));
    }

    @Test
    @DisplayName("INV-SRV-UT-026 - addMedicineCharges should reject empty item set")
    void addMedicineChargesShouldRejectEmptyItemSet() {
        Invoice invoice = invoice("PENDING", 200_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 200_000));
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice));

        assertThrows(IllegalArgumentException.class, () -> invoiceService.addMedicineCharges(invoiceId, Set.of(), List.of()));
    }

    @Test
    @DisplayName("INV-SRV-UT-027 - applyInsuranceDiscount should update totals")
    void applyInsuranceDiscountShouldUpdateTotals() {
        Invoice invoice = invoice("PENDING", 400_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 400_000));
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice));
        when(invoiceItemRepository.findByInvoiceId(invoiceId)).thenReturn(List.of(itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 400_000)));
        Set<InvoiceItemResponse> items = Set.of(InvoiceItemResponse.builder().id(existingItemId).quantity(1).unitPrice(400_000).description("Cạo vôi").serviceType("Dental").build());

        InvoiceResponseDTO result = invoiceService.applyInsuranceDiscount(invoiceId, insuranceClaimId, 100_000, items);

        assertAll(
                () -> assertEquals(100_000, result.getInsuranceTotalPay()),
                () -> assertEquals(300_000, result.getPatientTotalPay())
        );
    }

    @Test
    @DisplayName("INV-SRV-UT-028 - applyInsuranceDiscount should update matching invoice items")
    void applyInsuranceDiscountShouldUpdateMatchingInvoiceItems() {
        Invoice invoice = invoice("PENDING", 400_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 400_000));
        InvoiceItem existing = invoice.getItems().iterator().next();
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice));
        when(invoiceItemRepository.findByInvoiceId(invoiceId)).thenReturn(List.of(existing));
        Set<InvoiceItemResponse> items = Set.of(InvoiceItemResponse.builder().id(existingItemId).quantity(2).unitPrice(200_000).description("Cạo vôi").serviceType("Dental").build());

        invoiceService.applyInsuranceDiscount(invoiceId, insuranceClaimId, 100_000, items);

        assertEquals(2, existing.getQuantity());
    }

    @Test
    @DisplayName("INV-SRV-UT-029 - applyInsuranceDiscount should clamp final amount at zero")
    void applyInsuranceDiscountShouldClampFinalAmountAtZero() {
        Invoice invoice = invoice("PENDING", 100_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 100_000));
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice));
        when(invoiceItemRepository.findByInvoiceId(invoiceId)).thenReturn(List.of(itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 100_000)));
        Set<InvoiceItemResponse> items = Set.of(InvoiceItemResponse.builder().id(existingItemId).quantity(1).unitPrice(100_000).description("Cạo vôi").serviceType("Dental").build());

        InvoiceResponseDTO result = invoiceService.applyInsuranceDiscount(invoiceId, insuranceClaimId, 200_000, items);

        assertEquals(0, result.getPatientTotalPay());
    }

    @Test
    @DisplayName("INV-SRV-UT-030 - applyInsuranceDiscount should reject discounts greater than invoice total")
    void applyInsuranceDiscountShouldRejectDiscountGreaterThanInvoiceTotal() {
        Invoice invoice = invoice("PENDING", 100_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 100_000));
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice));
        when(invoiceItemRepository.findByInvoiceId(invoiceId)).thenReturn(List.of(itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 100_000)));
        Set<InvoiceItemResponse> items = Set.of(InvoiceItemResponse.builder().id(existingItemId).quantity(1).unitPrice(100_000).description("Cạo vôi").serviceType("Dental").build());

        assertThrows(IllegalArgumentException.class, () -> invoiceService.applyInsuranceDiscount(invoiceId, insuranceClaimId, 200_000, items));
    }

    @Test
    @DisplayName("INV-SRV-UT-031 - revertInsuranceDiscount should reset totals and clear claim")
    void revertInsuranceDiscountShouldResetTotalsAndClearClaim() {
        Invoice invoice = invoice("PENDING", 400_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 400_000));
        invoice.setInsuranceClaimId(insuranceClaimId);
        invoice.setInsuranceTotalPay(100_000);
        invoice.setPatientTotalPay(300_000);
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice));

        InvoiceResponseDTO result = invoiceService.revertInsuranceDiscount(invoiceId);

        assertAll(
                () -> assertEquals(0, result.getInsuranceTotalPay()),
                () -> assertEquals(400_000, result.getPatientTotalPay()),
                () -> assertNull(result.getInsuranceClaimId())
        );
    }

    @Test
    @DisplayName("INV-SRV-UT-032 - removeMedicineCharges should delete medicine items and recalculate totals")
    void removeMedicineChargesShouldDeleteMedicineItemsAndRecalculateTotals() {
        Invoice invoice = invoice("PENDING", 500_000,
                itemEntity(existingItemId, "DENTAL", "Cạo vôi", 1, 200_000),
                itemEntity(removedItemId, "MEDICINE", "Thuốc đau", 1, 300_000));
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice));
        when(invoiceItemRepository.findByInvoiceId(invoiceId)).thenReturn(List.of(itemEntity(existingItemId, "DENTAL", "Cạo vôi", 1, 200_000)));

        InvoiceResponseDTO result = invoiceService.removeMedicineCharges(invoiceId);

        assertEquals(200_000, result.getTotalAmount());
        verify(invoiceItemRepository).deleteByInvoice_IdAndServiceType(invoiceId, "MEDICINE");
    }

    @Test
    @DisplayName("INV-SRV-UT-033 - updateStatus should set paid status and paidAt")
    void updateStatusShouldSetPaidStatusAndPaidAt() {
        Invoice invoice = invoice("PENDING", 300_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 300_000));
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice));

        InvoiceResponseDTO result = invoiceService.updateStatus(invoiceId, "PAID");

        assertAll(
                () -> assertEquals("PAID", result.getStatus()),
                () -> assertNotNull(result.getPaidAt())
        );
    }

    @Test
    @DisplayName("INV-SRV-UT-034 - updateStatus should update arbitrary status")
    void updateStatusShouldUpdateArbitraryStatus() {
        Invoice invoice = invoice("PENDING", 300_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 300_000));
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice));

        InvoiceResponseDTO result = invoiceService.updateStatus(invoiceId, "CANCELLED");

        assertEquals("CANCELLED", result.getStatus());
    }

    @Test
    @DisplayName("INV-SRV-UT-035 - updateStatus should reject invalid status values")
    void updateStatusShouldRejectInvalidStatusValues() {
        Invoice invoice = invoice("PENDING", 300_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 300_000));
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice));

        assertThrows(IllegalArgumentException.class, () -> invoiceService.updateStatus(invoiceId, "INVALID"));
    }

    @Test
    @DisplayName("INV-SRV-UT-036 - canAddMedicineCharges should return true for pending invoice")
    void canAddMedicineChargesShouldReturnTrueForPendingInvoice() {
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice("PENDING", 300_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 300_000))));

        assertTrue(invoiceService.canAddMedicineCharges(invoiceId));
    }

    @Test
    @DisplayName("INV-SRV-UT-037 - canApplyInsuranceDiscount should return false for paid invoice")
    void canApplyInsuranceDiscountShouldReturnFalseForPaidInvoice() {
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice("PAID", 300_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 300_000))));

        assertFalse(invoiceService.canApplyInsuranceDiscount(invoiceId));
    }

    @Test
    @DisplayName("INV-SRV-UT-038 - canCancel should return true for pending invoice")
    void canCancelShouldReturnTrueForPendingInvoice() {
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice("PENDING", 300_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 300_000))));

        assertTrue(invoiceService.canCancel(invoiceId));
    }

    @Test
    @DisplayName("INV-SRV-UT-039 - canCancel should return false for paid invoice")
    void canCancelShouldReturnFalseForPaidInvoice() {
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice("PAID", 300_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 300_000))));

        assertFalse(invoiceService.canCancel(invoiceId));
    }

    @Test
    @DisplayName("INV-SRV-UT-040 - canMarkAsPaid should return true for pending invoice")
    void canMarkAsPaidShouldReturnTrueForPendingInvoice() {
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice("PENDING", 300_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 300_000))));

        assertTrue(invoiceService.canMarkAsPaid(invoiceId));
    }

    @Test
    @DisplayName("INV-SRV-UT-041 - canMarkAsPaid should return false for paid invoice")
    void canMarkAsPaidShouldReturnFalseForPaidInvoice() {
        when(invoiceRepository.findById(invoiceId)).thenReturn(java.util.Optional.of(invoice("PAID", 300_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 300_000))));

        assertFalse(invoiceService.canMarkAsPaid(invoiceId));
    }

    @Test
    @DisplayName("INV-SRV-UT-042 - addLabTestCharge should add a lab item to pending invoice")
    void addLabTestChargeShouldAddLabItemToPendingInvoice() {
        Invoice invoice = invoice("PENDING", 300_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 300_000));
        when(invoiceRepository.findAllByAppointmentId(appointmentId)).thenReturn(List.of(invoice));
        AddLabTestChargeRequestDTO request = new AddLabTestChargeRequestDTO(labTestId, appointmentId, 150_000, "Xét nghiệm máu");

        InvoiceResponseDTO result = invoiceService.addLabTestCharge(request);

        assertEquals(450_000, result.getTotalAmount());
    }

    @Test
    @DisplayName("INV-SRV-UT-043 - addLabTestCharge should use default description when missing")
    void addLabTestChargeShouldUseDefaultDescriptionWhenMissing() {
        Invoice invoice = invoice("PENDING", 300_000, itemEntity(existingItemId, "Dental", "Cạo vôi", 1, 300_000));
        when(invoiceRepository.findAllByAppointmentId(appointmentId)).thenReturn(List.of(invoice));
        AddLabTestChargeRequestDTO request = new AddLabTestChargeRequestDTO(labTestId, appointmentId, 150_000, null);

        InvoiceResponseDTO result = invoiceService.addLabTestCharge(request);

        assertTrue(result.getItems().stream().anyMatch(item -> "Phí xét nghiệm".equals(item.getDescription()) || "Phí xét nghiệm".equals(item.getDescription())));
    }

    private CreateInvoiceRequestDTO createRequest(UUID id, List<CreateInvoiceItemRequestDTO> items) {
        CreateInvoiceRequestDTO request = new CreateInvoiceRequestDTO();
        request.setId(id);
        request.setReceptionistId(receptionistId);
        request.setAppointmentId(appointmentId);
        request.setCurrency("VND");
        request.setItems(items);
        return request;
    }

    private CreateInvoiceRequestDTO updateRequest(List<CreateInvoiceItemRequestDTO> items) {
        CreateInvoiceRequestDTO request = createRequest(invoiceId, items);
        return request;
    }

    private List<CreateInvoiceItemRequestDTO> defaultCreateItems() {
        return List.of(
                itemDto(null, "Dental", "Cạo vôi", 1, 200_000),
                itemDto(null, "Dental", "Chụp X-quang", 1, 100_000)
        );
    }

    private List<CreateInvoiceItemRequestDTO> defaultUpdateItems() {
        return List.of(
                itemDto(existingItemId, "Dental", "Cạo vôi", 2, 200_000),
                itemDto(null, "Dental", "Tư vấn", 1, 150_000)
        );
    }

    private CreateInvoiceItemRequestDTO itemDto(UUID id, String serviceType, String description, int quantity, int unitPrice) {
        CreateInvoiceItemRequestDTO dto = new CreateInvoiceItemRequestDTO();
        dto.setId(id);
        dto.setServiceType(serviceType);
        dto.setDescription(description);
        dto.setQuantity(quantity);
        dto.setUnitPrice(unitPrice);
        return dto;
    }

    private Invoice invoice(String status, int totalAmount, InvoiceItem... items) {
        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setAppointmentId(appointmentId);
        invoice.setReceptionistId(receptionistId);
        invoice.setStatus(status);
        invoice.setCurrency("VND");
        invoice.setIssueAt(LocalDateTime.now().minusMinutes(5));
        invoice.setTotalAmount(totalAmount);
        invoice.setPatientTotalPay(totalAmount);
        invoice.setInsuranceTotalPay(0);
        for (InvoiceItem item : items) {
            invoice.addItem(item);
        }
        return invoice;
    }

    private InvoiceItem itemEntity(UUID id, String serviceType, String description, int quantity, int unitPrice) {
        InvoiceItem item = new InvoiceItem();
        item.setId(id);
        item.setServiceType(serviceType);
        item.setDescription(description);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setInsurancePayAmount(0);
        item.setPatientPayAmount(quantity * unitPrice);
        return item;
    }

    private Invoice toInvoice(CreateInvoiceRequestDTO request) {
        Invoice invoice = new Invoice();
        invoice.setId(request.getId());
        invoice.setReceptionistId(request.getReceptionistId());
        invoice.setAppointmentId(request.getAppointmentId());
        invoice.setCurrency(request.getCurrency());
        invoice.setStatus("NEW");
        invoice.setIssueAt(LocalDateTime.now().minusMinutes(1));
        invoice.setItems(new HashSet<>());
        if (request.getItems() != null) {
            for (CreateInvoiceItemRequestDTO dto : request.getItems()) {
                invoice.addItem(toInvoiceItem(dto));
            }
        }
        return invoice;
    }

    private InvoiceItem toInvoiceItem(CreateInvoiceItemRequestDTO dto) {
        InvoiceItem item = new InvoiceItem();
        item.setId(dto.getId());
        item.setReferenceId(dto.getReferenceId());
        item.setServiceType(dto.getServiceType());
        item.setQuantity(dto.getQuantity());
        item.setDescription(dto.getDescription());
        item.setUnitPrice(dto.getUnitPrice());
        item.setInsurancePayAmount(dto.getInsurancePayAmount());
        item.setPatientPayAmount(dto.getPatientPayAmount());
        return item;
    }

    private InvoiceResponseDTO toResponseDto(Invoice invoice) {
        InvoiceResponseDTO dto = new InvoiceResponseDTO();
        dto.setId(invoice.getId());
        dto.setReceptionistId(invoice.getReceptionistId() != null ? invoice.getReceptionistId().toString() : null);
        dto.setAppointmentId(invoice.getAppointmentId() != null ? invoice.getAppointmentId().toString() : null);
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setCurrency(invoice.getCurrency());
        dto.setStatus(invoice.getStatus());
        dto.setIssueAt(invoice.getIssueAt());
        dto.setPaidAt(invoice.getPaidAt());
        dto.setInsuranceTotalPay(invoice.getInsuranceTotalPay());
        dto.setPatientTotalPay(invoice.getPatientTotalPay());
        dto.setInsuranceClaimId(invoice.getInsuranceClaimId());
        dto.setUpdateAt(invoice.getUpdateAt());
        dto.setItems(invoice.getItems().stream().map(this::toResponseItem).collect(Collectors.toList()));
        return dto;
    }

    private InvoiceItemResponseDTO toResponseItem(InvoiceItem item) {
        InvoiceItemResponseDTO dto = new InvoiceItemResponseDTO();
        dto.setId(item.getId());
        dto.setReferenceId(item.getReferenceId());
        dto.setServiceType(item.getServiceType());
        dto.setQuantity(item.getQuantity());
        dto.setDescription(item.getDescription());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setInsurancePayAmount(item.getInsurancePayAmount());
        dto.setPatientPayAmount(item.getPatientPayAmount());
        dto.setClaimItemId(item.getClaimItemId());
        dto.setItemTotal(item.getQuantity() != null && item.getUnitPrice() != null ? item.getQuantity() * item.getUnitPrice() : null);
        return dto;
    }

    private AppointmentDTO appointment(UUID id) {
        return AppointmentDTO.builder()
                .id(id)
                .patientId(patientId)
                .appointmentStartTime(ZonedDateTime.now(ZoneOffset.UTC))
                .appointmentEndTime(ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(30))
                .build();
    }
}
