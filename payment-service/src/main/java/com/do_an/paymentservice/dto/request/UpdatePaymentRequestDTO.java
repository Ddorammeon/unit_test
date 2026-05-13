package com.do_an.paymentservice.dto.request;

import com.do_an.paymentservice.entity.PaymentStatus;
import lombok.Data;

@Data
public class UpdatePaymentRequestDTO {
    private Integer totalAmount;
    
    private PaymentStatus status;
    
    private String description;
}
