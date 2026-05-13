package com.do_an.paymentservice.aggregate;

import com.do_an.common.event.PaymentFailedEvent;
import com.do_an.common.event.PaymentProcessedEvent;
import com.do_an.common.event.PaymentStatusUpdatedEvent;
import com.do_an.paymentservice.entity.PaymentStatus;
import com.do_an.paymentservice.iservice.IPaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.GenericEventMessage;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventHandler {
    private final IPaymentService paymentService;
    private final EventBus eventBus;

    @EventHandler
    @Transactional
    public void on(PaymentStatusUpdatedEvent event) {
        log.info("Event Handler: Cập nhật Status {} -> {}", event.getPaymentId(), event.getStatus());

        UUID paymentId = event.getPaymentId();
        PaymentStatus newStatus = PaymentStatus.valueOf(event.getStatus());
        String reason = event.getReason();

        try {
            // Cập nhật payment status từ event
            paymentService.updatePaymentStatusFromEvent(paymentId, newStatus, reason);

            // Publish các event tương ứng dựa trên status
            if (newStatus == PaymentStatus.SUCCESSFUL) {
                eventBus.publish(GenericEventMessage.asEventMessage(
                        new PaymentProcessedEvent(
                                event.getPaymentId(),
                                event.getInvoiceId()
                        )
                ));
            } else if (newStatus == PaymentStatus.FAILED ||
                    newStatus == PaymentStatus.CANCELLED ||
                    newStatus == PaymentStatus.TIMEOUT) {
                eventBus.publish(GenericEventMessage.asEventMessage
                        (new PaymentFailedEvent(
                                        event.getPaymentId(),
                                        event.getStatus(),
                                        event.getReason()
                                )
                        ));
            }

        } catch (Exception e) {
            log.error("Lỗi khi cập nhật thanh toán {}: {}", paymentId, e.getMessage(), e);
        }
    }
}
