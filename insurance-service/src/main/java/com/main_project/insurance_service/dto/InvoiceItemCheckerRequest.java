package com.main_project.insurance_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemCheckerRequest {

    private UUID id;

    private UUID referenceId;
    private String serviceType;
    private Integer quantity;
    private String description;
    private Integer unitPrice;
}