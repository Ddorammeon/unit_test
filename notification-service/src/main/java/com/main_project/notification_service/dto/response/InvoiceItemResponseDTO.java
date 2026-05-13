package com.main_project.notification_service.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class InvoiceItemResponseDTO {
    private UUID id;
    private String itemName;
    private Integer quantity;
    private Integer unitPrice;
    private Integer totalPrice;
    private String itemType;
}

