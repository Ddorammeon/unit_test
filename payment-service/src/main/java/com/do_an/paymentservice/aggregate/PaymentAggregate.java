package com.do_an.paymentservice.aggregate;




import com.do_an.common.event.PaymentFailedEvent;
import com.do_an.common.event.PaymentInitiatedEvent;
import com.do_an.common.event.PaymentStatusUpdatedEvent;
import com.do_an.paymentservice.entity.PaymentStatus;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

@Aggregate
@NoArgsConstructor
@Slf4j
public class PaymentAggregate {

    @AggregateIdentifier
    private UUID paymentId;
    private UUID invoiceId;
    private PaymentStatus status;



    public PaymentAggregate(UUID paymentId, UUID invoiceId, UUID dispenseOrderId) {
        AggregateLifecycle.apply(new PaymentInitiatedEvent(
                paymentId,
                invoiceId,
                dispenseOrderId
        ));
    }

    //CẬP NHẬT TRẠNG THÁI (SUCCESS/FAILED)
    public void applyUpdatePaymentStatus(UUID paymentId, String status, String reason) {
            AggregateLifecycle.apply(new PaymentStatusUpdatedEvent(
                    paymentId,
                    this.invoiceId,
                    status,
                    reason

            ));
    }



    @EventSourcingHandler
    public void on(PaymentInitiatedEvent event) {
        this.paymentId = event.getPaymentId();
        this.invoiceId = event.getInvoiceId();
        this.status = PaymentStatus.PENDING;
    }

    @EventSourcingHandler
    public void on(PaymentStatusUpdatedEvent event) {
        this.status = PaymentStatus.valueOf(event.getStatus());

    }


}

