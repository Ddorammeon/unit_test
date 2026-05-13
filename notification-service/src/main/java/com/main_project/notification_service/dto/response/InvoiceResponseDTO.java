package com.main_project.notification_service.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class InvoiceResponseDTO {
    private UUID id;
    private String receptionistId;
    private String appointmentId;
    private Integer totalAmount;
    private String currency;
    private String status;
    private LocalDateTime issueAt;
    private LocalDateTime paidAt;
    private Integer insuranceTotalPay;
    private Integer patientTotalPay;
    private UUID insuranceClaimId;
    private LocalDateTime updateAt;
    private List<InvoiceItemResponseDTO> items;
}

