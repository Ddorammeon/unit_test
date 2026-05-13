package com.do_an.paymentservice.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentDomainModelTest {

    @Test
    @DisplayName("PAY-SRV-UT-047 - PaymentMethod CASH should expose readable description")
    void paymentMethodCashShouldExposeReadableDescription() {
        // Test Case ID: PAY-SRV-UT-047 | Objective: Verify CASH payment method exposes a human-readable description.
        assertTrue(PaymentMethod.CASH.getDescription() != null && !PaymentMethod.CASH.getDescription().isBlank());
    }

    @Test
    @DisplayName("PAY-SRV-UT-048 - PaymentMethod BANK_TRANSFER should expose readable description")
    void paymentMethodBankTransferShouldExposeReadableDescription() {
        // Test Case ID: PAY-SRV-UT-048 | Objective: Verify BANK_TRANSFER payment method exposes a human-readable description.
        assertTrue(PaymentMethod.BANK_TRANSFER.getDescription() != null && !PaymentMethod.BANK_TRANSFER.getDescription().isBlank());
    }

    @Test
    @DisplayName("PAY-SRV-UT-049 - PaymentStatus SUCCESSFUL should expose readable description")
    void paymentStatusSuccessfulShouldExposeReadableDescription() {
        // Test Case ID: PAY-SRV-UT-049 | Objective: Verify SUCCESSFUL payment status exposes a human-readable description.
        assertTrue(PaymentStatus.SUCCESSFUL.getDescription() != null && !PaymentStatus.SUCCESSFUL.getDescription().isBlank());
    }

    @Test
    @DisplayName("PAY-SRV-UT-050 - Payment entity builder should preserve core business fields")
    void paymentEntityBuilderShouldPreserveCoreBusinessFields() {
        // Test Case ID: PAY-SRV-UT-050 | Objective: Verify Payment entity builder preserves key fields used in payment workflows.
        UUID paymentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID invoiceId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        LocalDateTime paidAt = LocalDateTime.now();

        Payment payment = Payment.builder()
                .id(paymentId)
                .invoiceId(invoiceId)
                .totalAmount(500_000)
                .paymentMethod(PaymentMethod.CASH)
                .status(PaymentStatus.SUCCESSFUL)
                .transactionId("TX-01")
                .paidAt(paidAt)
                .description("cash at counter")
                .build();

        assertEquals(paymentId, payment.getId());
        assertEquals(invoiceId, payment.getInvoiceId());
        assertEquals(500_000, payment.getTotalAmount());
        assertEquals(PaymentStatus.SUCCESSFUL, payment.getStatus());
        assertNotNull(payment.getPaidAt());
    }
}
