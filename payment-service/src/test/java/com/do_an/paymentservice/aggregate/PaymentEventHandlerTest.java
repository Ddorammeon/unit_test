package com.do_an.paymentservice.aggregate;

import com.do_an.common.event.PaymentFailedEvent;
import com.do_an.common.event.PaymentProcessedEvent;
import com.do_an.common.event.PaymentStatusUpdatedEvent;
import com.do_an.paymentservice.entity.PaymentStatus;
import org.axonframework.eventhandling.EventMessage;
import com.do_an.paymentservice.iservice.IPaymentService;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.GenericEventMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentEventHandlerTest {

    @Mock
    private IPaymentService paymentService;
    @Mock
    private EventBus eventBus;

    @InjectMocks
    private PaymentEventHandler paymentEventHandler;

    private final UUID paymentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID invoiceId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Test
    @DisplayName("PAY-SRV-UT-035 - on(PaymentStatusUpdatedEvent) should update payment and publish PaymentProcessedEvent for SUCCESSFUL")
    void onPaymentStatusUpdatedShouldUpdatePaymentAndPublishProcessedEventForSuccessful() {
        // Test Case ID: PAY-SRV-UT-035 | Objective: Verify SUCCESSFUL status update persists payment and publishes PaymentProcessedEvent.
        paymentEventHandler.on(new PaymentStatusUpdatedEvent(paymentId, invoiceId, PaymentStatus.SUCCESSFUL.name(), "paid"));

        verify(paymentService).updatePaymentStatusFromEvent(paymentId, PaymentStatus.SUCCESSFUL, "paid");
        ArgumentCaptor<EventMessage<?>> captor = ArgumentCaptor.forClass(EventMessage.class);
        verify(eventBus).publish(captor.capture());
        Object payload = ((GenericEventMessage<?>) captor.getValue()).getPayload();
        assertInstanceOf(PaymentProcessedEvent.class, payload);
        assertEquals(invoiceId, ((PaymentProcessedEvent) payload).getInvoiceId());
    }

    @Test
    @DisplayName("PAY-SRV-UT-036 - on(PaymentStatusUpdatedEvent) should publish PaymentFailedEvent for FAILED")
    void onPaymentStatusUpdatedShouldPublishFailedEventForFailed() {
        // Test Case ID: PAY-SRV-UT-036 | Objective: Verify FAILED status update publishes PaymentFailedEvent.
        paymentEventHandler.on(new PaymentStatusUpdatedEvent(paymentId, invoiceId, PaymentStatus.FAILED.name(), "failed"));

        ArgumentCaptor<EventMessage<?>> captor = ArgumentCaptor.forClass(EventMessage.class);
        verify(eventBus).publish(captor.capture());
        Object payload = ((GenericEventMessage<?>) captor.getValue()).getPayload();
        assertInstanceOf(PaymentFailedEvent.class, payload);
        assertEquals(PaymentStatus.FAILED.name(), ((PaymentFailedEvent) payload).getStatus());
    }

    @Test
    @DisplayName("PAY-SRV-UT-037 - on(PaymentStatusUpdatedEvent) should publish PaymentFailedEvent for CANCELLED")
    void onPaymentStatusUpdatedShouldPublishFailedEventForCancelled() {
        // Test Case ID: PAY-SRV-UT-037 | Objective: Verify CANCELLED status update is propagated as a payment failure event.
        paymentEventHandler.on(new PaymentStatusUpdatedEvent(paymentId, invoiceId, PaymentStatus.CANCELLED.name(), "cancelled"));

        ArgumentCaptor<EventMessage<?>> captor = ArgumentCaptor.forClass(EventMessage.class);
        verify(eventBus).publish(captor.capture());
        Object payload = ((GenericEventMessage<?>) captor.getValue()).getPayload();
        assertInstanceOf(PaymentFailedEvent.class, payload);
        assertEquals(PaymentStatus.CANCELLED.name(), ((PaymentFailedEvent) payload).getStatus());
    }

    @Test
    @DisplayName("PAY-SRV-UT-038 - on(PaymentStatusUpdatedEvent) should not publish follow-up event for PENDING")
    void onPaymentStatusUpdatedShouldNotPublishFollowUpEventForPending() {
        // Test Case ID: PAY-SRV-UT-038 | Objective: Verify PENDING status only updates DB without publishing follow-up events.
        paymentEventHandler.on(new PaymentStatusUpdatedEvent(paymentId, invoiceId, PaymentStatus.PENDING.name(), "pending"));

        verify(paymentService).updatePaymentStatusFromEvent(paymentId, PaymentStatus.PENDING, "pending");
        verify(eventBus, never()).publish(any(EventMessage.class));
    }

    @Test
    @DisplayName("PAY-SRV-UT-039 - on(PaymentStatusUpdatedEvent) should swallow persistence exceptions")
    void onPaymentStatusUpdatedShouldSwallowPersistenceExceptions() {
        // Test Case ID: PAY-SRV-UT-039 | Objective: Verify handler does not rethrow when persistence update fails.
        doThrow(new RuntimeException("db down"))
                .when(paymentService).updatePaymentStatusFromEvent(paymentId, PaymentStatus.SUCCESSFUL, "paid");

        paymentEventHandler.on(new PaymentStatusUpdatedEvent(paymentId, invoiceId, PaymentStatus.SUCCESSFUL.name(), "paid"));

        verify(eventBus, never()).publish(any(EventMessage.class));
    }
}
