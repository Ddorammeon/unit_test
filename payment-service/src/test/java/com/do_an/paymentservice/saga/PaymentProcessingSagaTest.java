package com.do_an.paymentservice.saga;

import com.do_an.common.command.CancelInsuranceClaimCommand;
import com.do_an.common.command.CancelInvoiceCommand;
import com.do_an.common.command.MarkInvoiceAsPaidCommand;
import com.do_an.common.command.MarkPrescripAsSoldCommand;
import com.do_an.common.command.ReturnMedicineReservationCommand;
import com.do_an.common.command.UpdatePaymentStatusCommand;
import com.do_an.common.event.InvoiceCancelledEvent;
import com.do_an.common.event.PaymentFailedEvent;
import com.do_an.common.event.PaymentInitiatedEvent;
import com.do_an.common.event.PaymentProcessedEvent;
import com.do_an.paymentservice.client.InventoryClient;
import com.do_an.paymentservice.dto.response.DispenseOrderResponse;
import com.do_an.paymentservice.entity.PaymentStatus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentProcessingSagaTest {

    @Mock
    private CommandGateway commandGateway;
    @Mock
    private InventoryClient inventoryClient;
    @Mock
    private DeadlineManager deadlineManager;

    private PaymentProcessingSaga saga;

    private final UUID paymentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID invoiceId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID dispenseOrderId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private final UUID prescriptionId = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private final UUID insuranceClaimId = UUID.fromString("55555555-5555-5555-5555-555555555555");

    @BeforeEach
    void setUp() {
        saga = new PaymentProcessingSaga();
        ReflectionTestUtils.setField(saga, "commandGateway", commandGateway);
        ReflectionTestUtils.setField(saga, "inventoryClient", inventoryClient);
        ReflectionTestUtils.setField(saga, "deadlineManager", deadlineManager);
    }

    @Test
    @DisplayName("PAY-SRV-UT-026 - on(PaymentInitiatedEvent) should store ids and schedule payment deadline")
    void onPaymentInitiatedShouldStoreIdsAndSchedulePaymentDeadline() {
        // Test Case ID: PAY-SRV-UT-026 | Objective: Verify saga start stores payment context and schedules timeout deadline.
        when(deadlineManager.schedule(Duration.ofMinutes(30), "paymentSessionTimeout", paymentId)).thenReturn("deadline-1");

        invokeStart(new PaymentInitiatedEvent(paymentId, invoiceId, dispenseOrderId));

        assertEquals(paymentId, ReflectionTestUtils.getField(saga, "paymentId"));
        assertEquals(invoiceId, ReflectionTestUtils.getField(saga, "invoiceId"));
        assertEquals(dispenseOrderId, ReflectionTestUtils.getField(saga, "dispenseOrderId"));
        assertEquals("deadline-1", ReflectionTestUtils.getField(saga, "deadlineId"));
    }

    @Test
    @DisplayName("PAY-SRV-UT-027 - on(PaymentInitiatedEvent) should associate saga with invoice id")
    void onPaymentInitiatedShouldAssociateSagaWithInvoiceId() {
        // Test Case ID: PAY-SRV-UT-027 | Objective: Verify saga start associates the workflow with invoiceId.
        when(deadlineManager.schedule(Duration.ofMinutes(30), "paymentSessionTimeout", paymentId)).thenReturn("deadline-1");

        try (MockedStatic<SagaLifecycle> lifecycle = mockStatic(SagaLifecycle.class)) {
            saga.on(new PaymentInitiatedEvent(paymentId, invoiceId, dispenseOrderId));
            lifecycle.verify(() -> SagaLifecycle.associateWith("invoiceId", invoiceId.toString()));
        }
    }

    @Test
    @DisplayName("PAY-SRV-UT-028 - on(PaymentProcessedEvent) should cancel deadline and mark invoice paid")
    void onPaymentProcessedShouldCancelDeadlineAndMarkInvoicePaid() {
        // Test Case ID: PAY-SRV-UT-028 | Objective: Verify successful payment cancels timeout and marks invoice as paid.
        setSagaField("invoiceId", invoiceId);
        setSagaField("deadlineId", "deadline-1");

        saga.on(new PaymentProcessedEvent(paymentId, invoiceId));

        verify(deadlineManager).cancelSchedule("paymentSessionTimeout", "deadline-1");
        ArgumentCaptor<MarkInvoiceAsPaidCommand> captor = ArgumentCaptor.forClass(MarkInvoiceAsPaidCommand.class);
        verify(commandGateway).sendAndWait(captor.capture());
        assertEquals(invoiceId, captor.getValue().getInvoiceId());
        assertNull(ReflectionTestUtils.getField(saga, "deadlineId"));
    }

    @Test
    @DisplayName("PAY-SRV-UT-029 - on(PaymentFailedEvent) should cancel invoice when payment times out")
    void onPaymentFailedShouldCancelInvoiceWhenPaymentTimesOut() {
        // Test Case ID: PAY-SRV-UT-029 | Objective: Verify timeout failure triggers invoice cancellation flow.
        setSagaField("invoiceId", invoiceId);
        setSagaField("deadlineId", "deadline-1");

        saga.on(new PaymentFailedEvent(paymentId, PaymentStatus.TIMEOUT.name(), "timeout"));

        ArgumentCaptor<CancelInvoiceCommand> captor = ArgumentCaptor.forClass(CancelInvoiceCommand.class);
        verify(commandGateway).send(captor.capture());
        assertEquals(invoiceId, captor.getValue().getInvoiceId());
        assertNull(ReflectionTestUtils.getField(saga, "deadlineId"));
    }

    @Test
    @DisplayName("PAY-SRV-UT-030 - on(PaymentFailedEvent) should only cancel deadline for non-timeout failure")
    void onPaymentFailedShouldOnlyCancelDeadlineForNonTimeoutFailure() {
        // Test Case ID: PAY-SRV-UT-030 | Objective: Verify non-timeout failure cancels timeout but does not cancel invoice.
        setSagaField("deadlineId", "deadline-1");

        saga.on(new PaymentFailedEvent(paymentId, PaymentStatus.FAILED.name(), "bank rejected"));

        verify(deadlineManager).cancelSchedule("paymentSessionTimeout", "deadline-1");
        verify(commandGateway, never()).send(any(CancelInvoiceCommand.class));
        assertNull(ReflectionTestUtils.getField(saga, "deadlineId"));
    }

    @Test
    @DisplayName("PAY-SRV-UT-031 - on(InvoiceCancelledEvent) should return medicine reservation")
    void onInvoiceCancelledShouldReturnMedicineReservation() {
        // Test Case ID: PAY-SRV-UT-031 | Objective: Verify cancelled invoice triggers inventory reservation rollback.
        when(inventoryClient.getByPrescriptionId(prescriptionId))
                .thenReturn(new DispenseOrderResponse(dispenseOrderId, null, null, "CREATED", null, prescriptionId, null, null));

        saga.on(new InvoiceCancelledEvent(invoiceId, prescriptionId, null, "cancelled"));

        ArgumentCaptor<ReturnMedicineReservationCommand> captor = ArgumentCaptor.forClass(ReturnMedicineReservationCommand.class);
        verify(commandGateway).send(captor.capture());
        assertEquals(dispenseOrderId, captor.getValue().getDispenseOrderId());
        assertEquals(prescriptionId, captor.getValue().getPrescriptionId());
    }

    @Test
    @DisplayName("PAY-SRV-UT-032 - on(InvoiceCancelledEvent) should cancel insurance claim when claim id exists")
    void onInvoiceCancelledShouldCancelInsuranceClaimWhenClaimIdExists() {
        // Test Case ID: PAY-SRV-UT-032 | Objective: Verify cancelled invoice also cancels insurance claim when claim id exists.
        when(inventoryClient.getByPrescriptionId(prescriptionId))
                .thenReturn(new DispenseOrderResponse(dispenseOrderId, null, null, "CREATED", null, prescriptionId, null, null));

        saga.on(new InvoiceCancelledEvent(invoiceId, prescriptionId, insuranceClaimId, "cancelled"));

        ArgumentCaptor<CancelInsuranceClaimCommand> captor = ArgumentCaptor.forClass(CancelInsuranceClaimCommand.class);
        verify(commandGateway).send(captor.capture());
        assertEquals(insuranceClaimId, captor.getValue().getInsuranceClaimId());
        assertEquals(prescriptionId, captor.getValue().getPrescriptionId());
    }

    @Test
    @DisplayName("PAY-SRV-UT-033 - onTimeout() should send TIMEOUT payment status update")
    void onTimeoutShouldSendTimeoutPaymentStatusUpdate() {
        // Test Case ID: PAY-SRV-UT-033 | Objective: Verify timeout handler updates payment status to TIMEOUT.
        setSagaField("paymentId", paymentId);

        saga.onTimeout();

        ArgumentCaptor<UpdatePaymentStatusCommand> captor = ArgumentCaptor.forClass(UpdatePaymentStatusCommand.class);
        verify(commandGateway).send(captor.capture());
        assertEquals(paymentId, captor.getValue().getPaymentId());
        assertEquals(PaymentStatus.TIMEOUT.name(), captor.getValue().getStatus());
    }

    @Test
    @DisplayName("PAY-SRV-UT-034 - REAL FAIL business case: on(PaymentProcessedEvent) should mark prescription as sold when dispense order exists")
    void onPaymentProcessedShouldMarkPrescriptionAsSoldWhenDispenseOrderExists() {
        // Test Case ID: PAY-SRV-UT-034 | Objective: Verify successful payment should notify inventory to mark prescription as sold.
        setSagaField("invoiceId", invoiceId);
        setSagaField("dispenseOrderId", dispenseOrderId);
        setSagaField("deadlineId", "deadline-1");

        saga.on(new PaymentProcessedEvent(paymentId, invoiceId));

        verify(commandGateway).send(any(MarkPrescripAsSoldCommand.class));
    }

    private void invokeStart(PaymentInitiatedEvent event) {
        try (MockedStatic<SagaLifecycle> ignored = mockStatic(SagaLifecycle.class)) {
            saga.on(event);
        }
    }

    private void setSagaField(String name, Object value) {
        ReflectionTestUtils.setField(saga, name, value);
    }
}
