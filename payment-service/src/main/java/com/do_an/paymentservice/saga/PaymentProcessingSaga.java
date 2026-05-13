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
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.UUID;

@Saga
@Slf4j
public class PaymentProcessingSaga {


    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient InventoryClient inventoryClient;

    @Autowired
    private transient DeadlineManager deadlineManager;

    private UUID paymentId;
    private UUID invoiceId;
    private UUID dispenseOrderId;
    private String deadlineId;

    @StartSaga
    @SagaEventHandler(associationProperty = "paymentId")
    public void on(PaymentInitiatedEvent event) {
        this.paymentId = event.getPaymentId();
        this.invoiceId = event.getInvoiceId();
        this.dispenseOrderId = event.getDispenseOrderId();

        SagaLifecycle.associateWith("invoiceId", invoiceId.toString());
        log.info("Bắt đầu phiên thanh toán: {}", paymentId);

        // Đặt deadline cho phiên thanh toán
        this.deadlineId = deadlineManager.schedule(
                Duration.ofMinutes(30),
                "paymentSessionTimeout",
                this.paymentId
        );
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "paymentId")
    public void on(PaymentProcessedEvent event) {
        log.info("Thanh toán thành công. Cập nhật Invoice {} sang PAID.", event.getInvoiceId());

        cancelDeadline();

        // Đánh dấu Invoice là PAID
        commandGateway.sendAndWait(new MarkInvoiceAsPaidCommand(event.getInvoiceId()));




    }

    @SagaEventHandler(associationProperty = "paymentId")
    public void on(PaymentFailedEvent event) {
        boolean isTimeout = PaymentStatus.TIMEOUT.name().equals(event.getStatus());

        if (isTimeout) {
            log.info("🛑 Giao dịch TIMEOUT. Thực hiện Hủy Hóa Đơn và Trả Thuốc.");
            // Với TIMEOUT, để deadline handler đảm nhiệm việc gửi UpdatePaymentStatusCommand,
            // ở đây chỉ lo hủy hóa đơn và rollback các service khác
            commandGateway.send(new CancelInvoiceCommand(
                    this.invoiceId,
                    "Hủy do hết hạn thanh toán (Timeout)"
            ));
            // Không cần cancelDeadline vì deadline đã chạy
            this.deadlineId = null;
        } else {
            log.info("⚠️ Giao dịch thất bại do: {}. Giữ nguyên Hóa đơn để thử lại.", event.getReason());
            cancelDeadline();
        }
        // Notification cho frontend sẽ do notification-service lắng nghe các event tương ứng
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "invoiceId")
    public void on(InvoiceCancelledEvent event) {
        log.info("Hóa đơn {} đã chuyển sang CANCELLED. Thực hiện đồng bộ trạng thái các service khác...",
                event.getInvoiceId());

        // Lấy DispenseOrder theo prescriptionId
        DispenseOrderResponse dispenseOrderResponse =
                inventoryClient.getByPrescriptionId(event.getPrescriptionId());
        UUID dispenseOrderId = dispenseOrderResponse.getId();

        // Cập nhật Inventory: CANCELLED + trả lại số lượng
        commandGateway.send(new ReturnMedicineReservationCommand(
                dispenseOrderId,
                event.getPrescriptionId()
        ));

        // Cập nhật Insurance: Claim -> CANCELLED (nếu có claim)
        if (event.getInsuranceClaimId() != null) {
            commandGateway.send(new CancelInsuranceClaimCommand(
                    event.getInsuranceClaimId(),
                    event.getPrescriptionId(),
                    event.getReason()
            ));
        }
    }

    @DeadlineHandler(deadlineName = "paymentSessionTimeout")
    public void onTimeout() {
        log.info("Saga: Timeout {} phút. Tự động đánh dấu Payment là FAILED (TIMEOUT).",
                Duration.ofMinutes(30).toMinutes());

        commandGateway.send(new UpdatePaymentStatusCommand(
                paymentId,
                PaymentStatus.TIMEOUT.name(),
                "Thanh toán thất bại do mã hết hạn cho thử lại"
        ));
    }

    private void cancelDeadline() {
        if (deadlineId == null) {
            return;
        }
        try {
            deadlineManager.cancelSchedule("paymentSessionTimeout", deadlineId);
        } catch (Exception e) {
            log.debug("Deadline {} đã không còn tồn tại để hủy (có thể đã chạy xong).", deadlineId);
        }
        deadlineId = null;
    }
}
