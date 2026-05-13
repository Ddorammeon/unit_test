package com.do_an.invoiceservice.service;

import com.do_an.common.command.CancelInvoiceCommand;
import com.do_an.invoiceservice.entity.Invoice;
import com.do_an.invoiceservice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceCleanupService {

    private final InvoiceRepository invoiceRepository;
    private final CommandGateway commandGateway;

    // Chạy mỗi phút 1 lần
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredInvoices() {
        // Quy định: Đơn hàng tạo quá 30 phút mà chưa PAID thì hủy
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(30);

        // Tìm các hóa đơn thỏa mãn điều kiện: Trạng thái PENDING/WAITING và IssueAt < expirationTime
        // Bạn cần viết thêm query này trong Repository nếu chưa có
        List<Invoice> expiredInvoices = invoiceRepository.findAllByStatusAndIssueAtBefore(
                "PENDING", // Hoặc WAITING_FOR_PAYMENT tùy định nghĩa status của bạn
                expirationTime
        );

        if (!expiredInvoices.isEmpty()) {
            log.info("Tìm thấy {} hóa đơn quá hạn. Tiến hành hủy.", expiredInvoices.size());
        }

        for (Invoice invoice : expiredInvoices) {
            log.info("Auto-Cancelling Invoice: {}", invoice.getId());

            // Gửi lệnh Hủy -> Command này sẽ kích hoạt logic Rollback Inventory
            commandGateway.send(new CancelInvoiceCommand(
                    invoice.getId(),
                    "System Auto-Cancel: Payment Timeout (30m)"
            ));
        }
    }
}