package com.do_an.paymentservice.dto.response;

import com.do_an.paymentservice.entity.PaymentMethod;
import com.do_an.paymentservice.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentResponseDTO {

    private UUID paymentId; // ID của Payment record

    private UUID invoiceId; // Invoice ID

    private Integer totalAmount; // Số tiền

    private PaymentStatus status; // Trạng thái hiện tại

    private PaymentMethod paymentMethod; // Phương thức thanh toán

    private String transactionId; // Transaction ID (nếu có)
    private String description;

    /**
     * URL thanh toán - Client dùng để chuyển hướng
     * Ví dụ: QR code URL hoặc link VNPAY
     */
    private String paymentUrl;

    private String message; // Thông báo (ví dụ: "Thanh toán tiền mặt thành công")

    private LocalDateTime paidAt;
    private LocalDateTime expiredAt;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}
