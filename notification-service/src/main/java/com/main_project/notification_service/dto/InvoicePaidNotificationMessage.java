package com.main_project.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class InvoicePaidNotificationMessage {
    private String type;
    private String appointmentId;
    private String reason; // Có thể dùng để chứa invoiceId hoặc dispenseOrderId
    private String message;
    private String invoiceId; // ✅ THÊM
    private String dispenseOrderId; // ✅ THÊM (optional)
    private Long timestamp; // ✅ THÊM

    public InvoicePaidNotificationMessage(String type, String appointmentId, String reason, String message) {
        this.type = type;
        this.appointmentId = appointmentId;
        this.reason = reason;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
}
