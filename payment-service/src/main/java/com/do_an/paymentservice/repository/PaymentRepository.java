package com.do_an.paymentservice.repository;


import com.do_an.paymentservice.entity.Payment;
import com.do_an.paymentservice.entity.PaymentMethod;
import com.do_an.paymentservice.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    // Dùng để tra cứu khi Webhook gọi về
    Optional<Payment> findByTransactionId(String transactionId);

    // Dùng để client (app) polling kiểm tra trạng thái
    Optional<Payment> findFirstByInvoiceIdOrderByCreateAtDesc(UUID invoiceId);

    //Tìm tất cả payments theo invoice ID
    Optional<Payment> findByInvoiceIdAndId(UUID invoiceId, UUID paymentId);
    
    // Tìm tất cả payments theo invoice ID
    List<Payment> findAllByInvoiceId(UUID invoiceId);
    
    // Tìm tất cả payments theo status
    List<Payment> findAllByStatus(PaymentStatus status);
    
    // Tìm tất cả payments theo payment method
    List<Payment> findAllByPaymentMethod(PaymentMethod paymentMethod);
    
    // Tìm tất cả payments theo invoiceId và status
    List<Payment> findAllByInvoiceIdAndStatus(UUID invoiceId, PaymentStatus status);
    
    // Tìm tất cả payments theo invoiceId và paymentMethod
    List<Payment> findAllByInvoiceIdAndPaymentMethod(UUID invoiceId, PaymentMethod paymentMethod);
    
    // Tìm tất cả payments theo status và paymentMethod
    List<Payment> findAllByStatusAndPaymentMethod(PaymentStatus status, PaymentMethod paymentMethod);
    
    // Tìm tất cả payments theo invoiceId, status và paymentMethod
    List<Payment> findAllByInvoiceIdAndStatusAndPaymentMethod(
            UUID invoiceId,
            PaymentStatus status, 
            PaymentMethod paymentMethod);
    
    // Tìm tất cả payments, sắp xếp theo thời gian tạo giảm dần
    List<Payment> findAllByOrderByCreateAtDesc();

    // Tìm tất cả payments PENDING đã hết hạn
    List<Payment> findAllByStatusAndExpiredAtBefore(PaymentStatus status, LocalDateTime expiredAt);
}