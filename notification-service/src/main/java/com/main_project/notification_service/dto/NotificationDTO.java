package com.main_project.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private UUID id;
    private UUID userId;
    private String channel;
    private String templateId;
    private String message;
    private String status;
    private String errorMessage;
    private int retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

      // Thêm các field để map với InvoicePaidNotification
    private String invoiceId;
    private String dispenseOrderId;
    private String appointmentId;
    private String type; // INVOICE_PAID, etc.
    private Long timestamp;


}