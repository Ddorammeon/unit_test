package com.do_an.paymentservice.dto.request;

import com.do_an.paymentservice.entity.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreatePaymentRequestDTO {
    @NotNull
    private UUID invoiceId;
    
    // totalAmount là optional - sẽ được tính từ InvoiceItem nếu không có
    private Integer totalAmount;
    
    @NotNull
    private PaymentMethod paymentMethod; // "CASH" hoặc "BANK_TRANSFER"
}
