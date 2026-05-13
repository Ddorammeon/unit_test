package com.main_project.notification_service.dto.request;

import lombok.Data;

@Data
public class InvoicePaidRequest {
    private String invoiceId;
    private String appointmentId;
    private String message;
}
