package com.do_an.paymentservice.aggregate;

import com.do_an.common.command.CreatePaymentCommand;
import com.do_an.common.command.UpdatePaymentStatusCommand;
import com.do_an.paymentservice.iservice.IPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.modelling.command.Repository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCommandHandler {
    private final IPaymentService paymentService;
    private final Repository<PaymentAggregate> paymentAggregateRepository;

    @CommandHandler
    @Transactional
    public void handle(CreatePaymentCommand command) throws Exception{
        log.info("Xử lý CreatePaymentCommand - PaymentId: {}, InvoiceId: {}", 
                command.getPaymentId(), command.getInvoiceId());
        
        paymentAggregateRepository.newInstance(() -> new PaymentAggregate(
                command.getPaymentId(),
                command.getInvoiceId(),
                command.getDispenseOrderId()
        ));
    }

    @CommandHandler
    @Transactional
    public void handle(UpdatePaymentStatusCommand command) {
        log.info("Xử lý UpdatePaymentStatusCommand - PaymentId: {}, Status: {}", 
                command.getPaymentId(), command.getStatus());

        UUID paymentId = command.getPaymentId();
        
        // Kiểm tra xem có thể cập nhật status không
//        if (!paymentService.canUpdatePaymentStatus(paymentId)) {
//            log.warn("Payment {} đã thanh toán thành công, không thể cập nhật sang trạng thái khác.", paymentId);
//            return;
//        }

        // Cập nhật status thông qua aggregate
        paymentAggregateRepository.load(paymentId.toString())
                .execute(aggregate -> aggregate.applyUpdatePaymentStatus(
                        paymentId,
                        command.getStatus(),
                        command.getReason()
                ));
    }
}
