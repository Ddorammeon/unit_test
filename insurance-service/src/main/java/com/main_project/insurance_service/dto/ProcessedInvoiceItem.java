package com.main_project.insurance_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedInvoiceItem {
    private InvoiceItemDTO invoiceItem;
    private  UUID bhytCatalogueId;
}
