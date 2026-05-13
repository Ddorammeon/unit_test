package com.do_an.paymentservice.dto.response;

import lombok.Data;

import java.util.UUID;
@Data
public class InvoiceItemResponseDTO {
    private UUID id;
    private UUID referenceId;
    private String serviceType;
    private Integer quantity;
    private String description;
    private Integer unitPrice;
    private Integer insurancePayAmount;
    private Integer patientPayAmount;
    private UUID claimItemId;

    // Chúng ta có thể thêm một trường tính toán
    private Integer itemTotal;
}
