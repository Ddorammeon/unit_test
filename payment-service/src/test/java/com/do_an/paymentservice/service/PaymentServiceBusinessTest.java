package com.do_an.paymentservice.service;

import com.do_an.common.command.CreatePaymentCommand;
import com.do_an.common.command.UpdatePaymentStatusCommand;
import com.do_an.paymentservice.client.InventoryClient;
import com.do_an.paymentservice.client.InvoiceClient;
import com.do_an.paymentservice.client.PatientClient;
import com.do_an.paymentservice.dto.request.CreatePaymentRequestDTO;
import com.do_an.paymentservice.dto.request.UpdatePaymentRequestDTO;
import com.do_an.paymentservice.dto.response.DispenseOrderResponse;
import com.do_an.paymentservice.dto.response.InvoiceItemResponseDTO;
import com.do_an.paymentservice.dto.response.InvoiceResponseDTO;
import com.do_an.paymentservice.dto.response.MedicalHistoryResponseDTO;
import com.do_an.paymentservice.dto.response.PaymentResponseDTO;
import com.do_an.paymentservice.entity.Payment;
import com.do_an.paymentservice.entity.PaymentMethod;
import com.do_an.paymentservice.entity.PaymentStatus;
import com.do_an.paymentservice.exception.PaymentNotFoundException;
import com.do_an.paymentservice.mapper.PaymentMapper;
import com.do_an.paymentservice.repository.PaymentRepository;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.PaymentData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceBusinessTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PayOS payOS;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private InventoryClient inventoryClient;
    @Mock
    private InvoiceClient invoiceClient;
    @Mock
    private PatientClient patientClient;
    @Mock
    private CommandGateway commandGateway;
    @Mock
    private EventBus eventBus;
    @Mock
    private CheckoutResponseData checkoutResponseData;

    private PaymentService paymentService;

    private final UUID invoiceId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID paymentId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID appointmentId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private final UUID medicalHistoryId = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private final UUID dispenseOrderId = UUID.fromString("55555555-5555-5555-5555-555555555555");

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                paymentRepository,
                payOS,
                paymentMapper,
                inventoryClient,
                invoiceClient,
                patientClient,
                commandGateway,
                eventBus
        );
        ReflectionTestUtils.setField(paymentService, "returnUrl", "http://localhost/payment-success");
        ReflectionTestUtils.setField(paymentService, "cancelUrl", "http://localhost/payment-cancel");

        lenient().when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            if (payment.getId() == null) {
                payment.setId(paymentId);
            }
            return payment;
        });

        lenient().when(paymentMapper.toResponseDto(any(Payment.class))).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));
    }

    @Test
    @DisplayName("PAY-SRV-UT-001 - initiatePayment should use invoice patientTotalPay for CASH")
    void initiatePaymentShouldUseInvoicePatientTotalPayForCash() {
        // Test Case ID: PAY-SRV-UT-001 | Objective: Verify CASH initiation uses invoice patientTotalPay as payment amount.
        when(invoiceClient.getInvoiceById(invoiceId)).thenReturn(invoice("PENDING", 1_500_000, dentalItem(1, 1_500_000)));

        PaymentResponseDTO result = paymentService.initiatePayment(createRequest(PaymentMethod.CASH, null));

        assertAll(
                () -> assertEquals(invoiceId, result.getInvoiceId()),
                () -> assertEquals(1_500_000, result.getTotalAmount()),
                () -> assertEquals(PaymentMethod.CASH, result.getPaymentMethod()),
                () -> assertEquals(PaymentStatus.PENDING, result.getStatus())
        );
    }

    @Test
    @DisplayName("PAY-SRV-UT-002 - initiatePayment should dispatch create and success commands for CASH")
    void initiatePaymentShouldDispatchCreateAndSuccessCommandsForCash() {
        // Test Case ID: PAY-SRV-UT-002 | Objective: Verify CASH initiation dispatches create and status update commands.
        when(invoiceClient.getInvoiceById(invoiceId)).thenReturn(invoice("DRAFT", 900_000, dentalItem(1, 900_000)));

        paymentService.initiatePayment(createRequest(PaymentMethod.CASH, null));

        verify(commandGateway).sendAndWait(any(CreatePaymentCommand.class));
        verify(commandGateway).send(any(UpdatePaymentStatusCommand.class));
    }

    @Test
    @DisplayName("PAY-SRV-UT-003 - initiatePayment should include dispense order id when inventory data exists")
    void initiatePaymentShouldIncludeDispenseOrderIdWhenInventoryDataExists() {
        // Test Case ID: PAY-SRV-UT-003 | Objective: Verify create payment command includes dispenseOrderId when inventory data is found.
        when(invoiceClient.getInvoiceById(invoiceId)).thenReturn(invoice("PENDING", 600_000, medicineItem(2, 300_000)));
        when(patientClient.getByAppointment(appointmentId)).thenReturn(List.of(new MedicalHistoryResponseDTO(medicalHistoryId, appointmentId, null, null, null, null, null)));
        when(inventoryClient.getByMedicalHistoryId(medicalHistoryId)).thenReturn(ResponseEntity.ok(new DispenseOrderResponse(dispenseOrderId, null, null, "CREATED", medicalHistoryId, null, null, null)));

        paymentService.initiatePayment(createRequest(PaymentMethod.CASH, null));

        ArgumentCaptor<CreatePaymentCommand> captor = ArgumentCaptor.forClass(CreatePaymentCommand.class);
        verify(commandGateway).sendAndWait(captor.capture());
        assertEquals(dispenseOrderId, captor.getValue().getDispenseOrderId());
    }

    @Test
    @DisplayName("PAY-SRV-UT-004 - initiatePayment should continue when patient service returns no medical history")
    void initiatePaymentShouldContinueWhenPatientServiceReturnsNoMedicalHistory() {
        // Test Case ID: PAY-SRV-UT-004 | Objective: Verify payment initiation still succeeds when patient-service returns no medical history.
        when(invoiceClient.getInvoiceById(invoiceId)).thenReturn(invoice("PENDING", 500_000, dentalItem(1, 500_000)));
        when(patientClient.getByAppointment(appointmentId)).thenReturn(List.of());

        PaymentResponseDTO result = paymentService.initiatePayment(createRequest(PaymentMethod.CASH, null));

        assertEquals(PaymentMethod.CASH, result.getPaymentMethod());
    }

    @Test
    @DisplayName("PAY-SRV-UT-005 - initiatePayment should continue when inventory lookup fails")
    void initiatePaymentShouldContinueWhenInventoryLookupFails() {
        // Test Case ID: PAY-SRV-UT-005 | Objective: Verify payment initiation still succeeds when inventory lookup throws an exception.
        when(invoiceClient.getInvoiceById(invoiceId)).thenReturn(invoice("PENDING", 700_000, medicineItem(1, 700_000)));
        when(patientClient.getByAppointment(appointmentId)).thenReturn(List.of(new MedicalHistoryResponseDTO(medicalHistoryId, appointmentId, null, null, null, null, null)));
        when(inventoryClient.getByMedicalHistoryId(medicalHistoryId)).thenThrow(new RuntimeException("inventory down"));

        PaymentResponseDTO result = paymentService.initiatePayment(createRequest(PaymentMethod.CASH, null));

        assertEquals(700_000, result.getTotalAmount());
    }

    @Test
    @DisplayName("PAY-SRV-UT-006 - initiatePayment should create PayOS link for BANK_TRANSFER")
    void initiatePaymentShouldCreatePayOsLinkForBankTransfer() throws Exception {
        // Test Case ID: PAY-SRV-UT-006 | Objective: Verify BANK_TRANSFER initiation creates checkout URL and transaction metadata.
        when(invoiceClient.getInvoiceById(invoiceId)).thenReturn(invoice("PENDING", 1_200_000, dentalItem(2, 600_000)));
        when(checkoutResponseData.getCheckoutUrl()).thenReturn("https://pay.payos.vn/checkout");
        when(payOS.createPaymentLink(any(PaymentData.class))).thenReturn(checkoutResponseData);

        PaymentResponseDTO result = paymentService.initiatePayment(createRequest(PaymentMethod.BANK_TRANSFER, null));

        assertAll(
                () -> assertEquals(PaymentMethod.BANK_TRANSFER, result.getPaymentMethod()),
                () -> assertEquals(PaymentStatus.PENDING, result.getStatus()),
                () -> assertEquals("https://pay.payos.vn/checkout", result.getPaymentUrl()),
                () -> assertNotNull(result.getTransactionId()),
                () -> assertNotNull(result.getExpiredAt())
        );
    }

    @Test
    @DisplayName("PAY-SRV-UT-007 - initiatePayment should dispatch create command asynchronously for BANK_TRANSFER")
    void initiatePaymentShouldDispatchCreateCommandAsynchronouslyForBankTransfer() throws Exception {
        // Test Case ID: PAY-SRV-UT-007 | Objective: Verify BANK_TRANSFER uses async create command instead of sendAndWait.
        when(invoiceClient.getInvoiceById(invoiceId)).thenReturn(invoice("DRAFT", 800_000, dentalItem(1, 800_000)));
        when(checkoutResponseData.getCheckoutUrl()).thenReturn("https://pay.payos.vn/checkout");
        when(payOS.createPaymentLink(any(PaymentData.class))).thenReturn(checkoutResponseData);

        paymentService.initiatePayment(createRequest(PaymentMethod.BANK_TRANSFER, null));

        verify(commandGateway).send(any(CreatePaymentCommand.class));
        verify(commandGateway, never()).sendAndWait(any(CreatePaymentCommand.class));
    }

    @Test
    @DisplayName("PAY-SRV-UT-008 - initiatePayment should convert medicine item into PayOS payment item")
    void initiatePaymentShouldConvertMedicineItemIntoPayOsPaymentItem() throws Exception {
        // Test Case ID: PAY-SRV-UT-008 | Objective: Verify medicine invoice items can be converted into PayOS items successfully.
        when(invoiceClient.getInvoiceById(invoiceId)).thenReturn(invoice("PENDING", 300_000, medicineItem(3, 100_000)));
        when(checkoutResponseData.getCheckoutUrl()).thenReturn("https://pay.payos.vn/medicine");
        when(payOS.createPaymentLink(any(PaymentData.class))).thenReturn(checkoutResponseData);

        PaymentResponseDTO result = paymentService.initiatePayment(createRequest(PaymentMethod.BANK_TRANSFER, null));

        assertEquals("https://pay.payos.vn/medicine", result.getPaymentUrl());
    }

    @Test
    @DisplayName("PAY-SRV-UT-009 - initiatePayment should reject invoice without items")
    void initiatePaymentShouldRejectInvoiceWithoutItems() {
        // Test Case ID: PAY-SRV-UT-009 | Objective: Verify payment initiation fails when invoice contains no billable items.
        when(invoiceClient.getInvoiceById(invoiceId)).thenReturn(invoice("PENDING", 100_000));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paymentService.initiatePayment(createRequest(PaymentMethod.CASH, null)));

        assertTrue(exception.getMessage().contains("Kh"));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("PAY-SRV-UT-010 - initiatePayment should reject invoice already paid")
    void initiatePaymentShouldRejectInvoiceAlreadyPaid() {
        // Test Case ID: PAY-SRV-UT-010 | Objective: Verify payment initiation fails when invoice is already PAID.
        when(invoiceClient.getInvoiceById(invoiceId)).thenReturn(invoice("PAID", 100_000, dentalItem(1, 100_000)));

        assertThrows(RuntimeException.class,
                () -> paymentService.initiatePayment(createRequest(PaymentMethod.CASH, null)));
    }

    @Test
    @DisplayName("PAY-SRV-UT-011 - initiatePayment should reject cancelled invoice")
    void initiatePaymentShouldRejectCancelledInvoice() {
        // Test Case ID: PAY-SRV-UT-011 | Objective: Verify payment initiation fails when invoice is CANCELLED.
        when(invoiceClient.getInvoiceById(invoiceId)).thenReturn(invoice("CANCELLED", 100_000, dentalItem(1, 100_000)));

        assertThrows(RuntimeException.class,
                () -> paymentService.initiatePayment(createRequest(PaymentMethod.BANK_TRANSFER, null)));
    }

    @Test
    @DisplayName("PAY-SRV-UT-012 - initiatePayment should fail when invoice service lookup fails")
    void initiatePaymentShouldFailWhenInvoiceServiceLookupFails() {
        // Test Case ID: PAY-SRV-UT-012 | Objective: Verify payment initiation surfaces invoice-service failures.
        when(invoiceClient.getInvoiceById(invoiceId)).thenThrow(new RuntimeException("invoice down"));

        assertThrows(RuntimeException.class,
                () -> paymentService.initiatePayment(createRequest(PaymentMethod.CASH, null)));
    }

    @Test
    @DisplayName("PAY-SRV-UT-013 - initiatePayment should fail when PayOS link creation fails")
    void initiatePaymentShouldFailWhenPayOsLinkCreationFails() throws Exception {
        // Test Case ID: PAY-SRV-UT-013 | Objective: Verify BANK_TRANSFER initiation fails when PayOS throws during link creation.
        when(invoiceClient.getInvoiceById(invoiceId)).thenReturn(invoice("PENDING", 100_000, dentalItem(1, 100_000)));
        when(payOS.createPaymentLink(any(PaymentData.class))).thenThrow(new RuntimeException("payos down"));

        assertThrows(RuntimeException.class,
                () -> paymentService.initiatePayment(createRequest(PaymentMethod.BANK_TRANSFER, null)));
    }

    @Test
    @DisplayName("PAY-SRV-UT-014 - getPaymentStatusOfInvoice should return latest payment")
    void getPaymentStatusOfInvoiceShouldReturnLatestPayment() {
        // Test Case ID: PAY-SRV-UT-014 | Objective: Verify polling API returns the latest payment record for an invoice.
        when(paymentRepository.findFirstByInvoiceIdOrderByCreateAtDesc(invoiceId)).thenReturn(Optional.of(payment(PaymentStatus.PENDING, PaymentMethod.BANK_TRANSFER)));

        PaymentResponseDTO result = paymentService.getPaymentStatusOfInvoice(invoiceId);

        assertEquals(paymentId, result.getPaymentId());
    }

    @Test
    @DisplayName("PAY-SRV-UT-015 - getPaymentById should return mapped payment details")
    void getPaymentByIdShouldReturnMappedPaymentDetails() {
        // Test Case ID: PAY-SRV-UT-015 | Objective: Verify getPaymentById returns mapped payment details for an existing payment.
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment(PaymentStatus.PENDING, PaymentMethod.CASH)));

        PaymentResponseDTO result = paymentService.getPaymentById(paymentId);

        assertEquals(paymentId, result.getPaymentId());
    }

    @Test
    @DisplayName("PAY-SRV-UT-016 - updatePayment should set paidAt and mark invoice paid when status becomes SUCCESSFUL")
    void updatePaymentShouldSetPaidAtAndMarkInvoicePaidWhenStatusBecomesSuccessful() {
        // Test Case ID: PAY-SRV-UT-016 | Objective: Verify updatePayment sets paidAt and notifies invoice-service when status becomes SUCCESSFUL.
        Payment payment = payment(PaymentStatus.PENDING, PaymentMethod.CASH);
        payment.setPaidAt(null);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        PaymentResponseDTO result = paymentService.updatePayment(paymentId, updateRequest(null, PaymentStatus.SUCCESSFUL, null));

        assertAll(
                () -> assertEquals(PaymentStatus.SUCCESSFUL, result.getStatus()),
                () -> assertNotNull(result.getPaidAt())
        );
        verify(invoiceClient).markAsPaid(invoiceId);
    }

    @Test
    @DisplayName("PAY-SRV-UT-017 - updatePayment should ignore blank description")
    void updatePaymentShouldIgnoreBlankDescription() {
        // Test Case ID: PAY-SRV-UT-017 | Objective: Verify blank description does not overwrite existing payment description.
        Payment payment = payment(PaymentStatus.PENDING, PaymentMethod.CASH);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        PaymentResponseDTO result = paymentService.updatePayment(paymentId, updateRequest(null, null, ""));

        assertEquals("payment description", result.getMessage());
    }

    @Test
    @DisplayName("PAY-SRV-UT-018 - validateInvoice should return invoice when invoice is billable")
    void validateInvoiceShouldReturnInvoiceWhenInvoiceIsBillable() {
        // Test Case ID: PAY-SRV-UT-018 | Objective: Verify validateInvoice returns invoice details when invoice has valid billable items.
        when(invoiceClient.getInvoiceById(invoiceId)).thenReturn(invoice("PENDING", 400_000, dentalItem(2, 200_000)));

        InvoiceResponseDTO result = paymentService.validateInvoice(invoiceId);

        assertEquals(invoiceId, result.getId());
    }

    @Test
    @DisplayName("PAY-SRV-UT-019 - deletePayment should reject successful payment")
    void deletePaymentShouldRejectSuccessfulPayment() {
        // Test Case ID: PAY-SRV-UT-019 | Objective: Verify deletePayment refuses to delete a successful payment.
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment(PaymentStatus.SUCCESSFUL, PaymentMethod.CASH)));

        assertThrows(IllegalStateException.class, () -> paymentService.deletePayment(paymentId));
    }

    @Test
    @DisplayName("PAY-SRV-UT-020 - REAL FAIL business case: CASH payment should become successful immediately")
    void cashPaymentShouldBecomeSuccessfulImmediately() {
        // Test Case ID: PAY-SRV-UT-020 | Objective: Verify cash-at-counter payments are returned as SUCCESSFUL immediately instead of PENDING.
        when(invoiceClient.getInvoiceById(invoiceId)).thenReturn(invoice("PENDING", 500_000, dentalItem(1, 500_000)));

        PaymentResponseDTO result = paymentService.initiatePayment(createRequest(PaymentMethod.CASH, null));

        assertEquals(PaymentStatus.SUCCESSFUL, result.getStatus());
    }

    @Test
    @DisplayName("PAY-SRV-UT-021 - REAL FAIL business case: initiatePayment should reject mismatched client total")
    void initiatePaymentShouldRejectMismatchedClientTotal() {
        // Test Case ID: PAY-SRV-UT-021 | Objective: Verify service rejects client totals that do not match invoice amount.
        when(invoiceClient.getInvoiceById(invoiceId)).thenReturn(invoice("PENDING", 900_000, dentalItem(1, 900_000)));

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.initiatePayment(createRequest(PaymentMethod.CASH, 1)));
    }

    @Test
    @DisplayName("PAY-SRV-UT-022 - REAL FAIL business case: initiatePayment should reject null payment method")
    void initiatePaymentShouldRejectNullPaymentMethod() throws Exception {
        // Test Case ID: PAY-SRV-UT-022 | Objective: Verify service-level validation rejects null paymentMethod.
        when(invoiceClient.getInvoiceById(invoiceId)).thenReturn(invoice("PENDING", 300_000, dentalItem(1, 300_000)));
        when(checkoutResponseData.getCheckoutUrl()).thenReturn("https://pay.payos.vn/invalid");
        when(payOS.createPaymentLink(any(PaymentData.class))).thenReturn(checkoutResponseData);

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.initiatePayment(createRequest(null, null)));
    }

    @Test
    @DisplayName("PAY-SRV-UT-023 - REAL FAIL business case: handlePayOSWebhook should persist SUCCESSFUL status locally")
    void handlePayOsWebhookShouldPersistSuccessfulStatusLocally() {
        // Test Case ID: PAY-SRV-UT-023 | Objective: Verify success webhook updates local payment row to SUCCESSFUL before returning.
        Payment payment = payment(PaymentStatus.PENDING, PaymentMethod.BANK_TRANSFER);
        payment.setTransactionId("123");
        when(paymentRepository.findByTransactionId("123")).thenReturn(Optional.of(payment));

        paymentService.handlePayOSWebhook("123", true, "BANK-123");

        assertEquals(PaymentStatus.SUCCESSFUL, payment.getStatus());
    }

    @Test
    @DisplayName("PAY-SRV-UT-024 - REAL FAIL business case: validateInvoice should reject paid invoice")
    void validateInvoiceShouldRejectPaidInvoice() {
        // Test Case ID: PAY-SRV-UT-024 | Objective: Verify validateInvoice blocks invoices already marked PAID.
        when(invoiceClient.getInvoiceById(invoiceId)).thenReturn(invoice("PAID", 400_000, dentalItem(1, 400_000)));

        assertThrows(IllegalStateException.class, () -> paymentService.validateInvoice(invoiceId));
    }

    @Test
    @DisplayName("PAY-SRV-UT-025 - REAL FAIL business case: callback success should mark invoice paid")
    void callbackSuccessShouldMarkInvoicePaid() {
        // Test Case ID: PAY-SRV-UT-025 | Objective: Verify successful redirect callback notifies invoice-service to mark the invoice as paid.
        Payment payment = payment(PaymentStatus.PENDING, PaymentMethod.BANK_TRANSFER);
        payment.setTransactionId("999");
        when(paymentRepository.findByTransactionId("999")).thenReturn(Optional.of(payment));

        paymentService.handlePaymentCallback("999", "PAID", "00", false);

        verify(invoiceClient).markAsPaid(invoiceId);
    }

    private CreatePaymentRequestDTO createRequest(PaymentMethod method, Integer totalAmount) {
        CreatePaymentRequestDTO request = new CreatePaymentRequestDTO();
        request.setInvoiceId(invoiceId);
        request.setPaymentMethod(method);
        request.setTotalAmount(totalAmount);
        return request;
    }

    private UpdatePaymentRequestDTO updateRequest(Integer totalAmount, PaymentStatus status, String description) {
        UpdatePaymentRequestDTO request = new UpdatePaymentRequestDTO();
        request.setTotalAmount(totalAmount);
        request.setStatus(status);
        request.setDescription(description);
        return request;
    }

    private PaymentResponseDTO toResponse(Payment payment) {
        return PaymentResponseDTO.builder()
                .paymentId(payment.getId())
                .invoiceId(payment.getInvoiceId())
                .totalAmount(payment.getTotalAmount())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .paymentUrl(payment.getPaymentUrl())
                .message(payment.getDescription())
                .paidAt(payment.getPaidAt())
                .expiredAt(payment.getExpiredAt())
                .createAt(payment.getCreateAt())
                .updateAt(payment.getUpdateAt())
                .build();
    }

    private Payment payment(PaymentStatus status, PaymentMethod method) {
        return Payment.builder()
                .id(paymentId)
                .invoiceId(invoiceId)
                .totalAmount(100_000)
                .paymentMethod(method)
                .status(status)
                .paidAt(status == PaymentStatus.SUCCESSFUL ? LocalDateTime.now() : null)
                .description("payment description")
                .build();
    }

    private InvoiceResponseDTO invoice(String status, int patientTotalPay, InvoiceItemResponseDTO... items) {
        InvoiceResponseDTO invoice = new InvoiceResponseDTO();
        invoice.setId(invoiceId);
        invoice.setAppointmentId(appointmentId);
        invoice.setStatus(status);
        invoice.setPatientTotalPay(patientTotalPay);
        invoice.setTotalAmount(patientTotalPay);
        invoice.setItems(List.of(items));
        return invoice;
    }

    private InvoiceItemResponseDTO dentalItem(int quantity, int unitPrice) {
        return item("Dental", "Tram rang", quantity, unitPrice);
    }

    private InvoiceItemResponseDTO medicineItem(int quantity, int unitPrice) {
        return item("Medicine", "Paracetamol", quantity, unitPrice);
    }

    private InvoiceItemResponseDTO item(String serviceType, String description, int quantity, int unitPrice) {
        InvoiceItemResponseDTO item = new InvoiceItemResponseDTO();
        item.setId(UUID.randomUUID());
        item.setServiceType(serviceType);
        item.setDescription(description);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setItemTotal(quantity * unitPrice);
        return item;
    }
}
