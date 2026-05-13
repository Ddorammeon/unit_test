package com.do_an.paymentservice.aggregate;

import com.do_an.common.event.PaymentInitiatedEvent;
import com.do_an.common.event.PaymentStatusUpdatedEvent;
import com.do_an.paymentservice.entity.PaymentStatus;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

class PaymentAggregateTest {

    private final UUID paymentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID invoiceId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID dispenseOrderId = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Test
    @DisplayName("PAY-SRV-UT-043 - PaymentAggregate constructor should apply PaymentInitiatedEvent")
    void paymentAggregateConstructorShouldApplyPaymentInitiatedEvent() {
        // Test Case ID: PAY-SRV-UT-043 | Objective: Verify aggregate constructor emits PaymentInitiatedEvent with the same ids.
        try (MockedStatic<AggregateLifecycle> lifecycle = mockStatic(AggregateLifecycle.class)) {
            new PaymentAggregate(paymentId, invoiceId, dispenseOrderId);
            lifecycle.verify(() -> AggregateLifecycle.apply(new PaymentInitiatedEvent(paymentId, invoiceId, dispenseOrderId)));
        }
    }

    @Test
    @DisplayName("PAY-SRV-UT-044 - applyUpdatePaymentStatus should apply PaymentStatusUpdatedEvent")
    void applyUpdatePaymentStatusShouldApplyPaymentStatusUpdatedEvent() {
        // Test Case ID: PAY-SRV-UT-044 | Objective: Verify aggregate emits PaymentStatusUpdatedEvent when status update is requested.
        PaymentAggregate aggregate = new PaymentAggregate();
        ReflectionTestUtils.setField(aggregate, "invoiceId", invoiceId);

        try (MockedStatic<AggregateLifecycle> lifecycle = mockStatic(AggregateLifecycle.class)) {
            aggregate.applyUpdatePaymentStatus(paymentId, "SUCCESSFUL", "paid");
            lifecycle.verify(() -> AggregateLifecycle.apply(new PaymentStatusUpdatedEvent(paymentId, invoiceId, "SUCCESSFUL", "paid")));
        }
    }

    @Test
    @DisplayName("PAY-SRV-UT-045 - on(PaymentInitiatedEvent) should set aggregate state to PENDING")
    void onPaymentInitiatedEventShouldSetAggregateStateToPending() {
        // Test Case ID: PAY-SRV-UT-045 | Objective: Verify event sourcing handler initializes aggregate state as PENDING.
        PaymentAggregate aggregate = new PaymentAggregate();

        aggregate.on(new PaymentInitiatedEvent(paymentId, invoiceId, dispenseOrderId));

        assertEquals(paymentId, ReflectionTestUtils.getField(aggregate, "paymentId"));
        assertEquals(invoiceId, ReflectionTestUtils.getField(aggregate, "invoiceId"));
        assertEquals(PaymentStatus.PENDING, ReflectionTestUtils.getField(aggregate, "status"));
    }

    @Test
    @DisplayName("PAY-SRV-UT-046 - on(PaymentStatusUpdatedEvent) should update aggregate status")
    void onPaymentStatusUpdatedEventShouldUpdateAggregateStatus() {
        // Test Case ID: PAY-SRV-UT-046 | Objective: Verify event sourcing handler updates aggregate status from status event.
        PaymentAggregate aggregate = new PaymentAggregate();

        aggregate.on(new PaymentStatusUpdatedEvent(paymentId, invoiceId, PaymentStatus.FAILED.name(), "rejected"));

        assertEquals(PaymentStatus.FAILED, ReflectionTestUtils.getField(aggregate, "status"));
    }
}
