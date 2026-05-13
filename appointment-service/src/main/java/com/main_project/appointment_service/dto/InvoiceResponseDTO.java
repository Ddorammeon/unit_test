package com.main_project.appointment_service.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class InvoiceResponseDTO {
    private UUID id;
    private UUID receptionistId;
    private UUID appointmentId;
    private Integer totalAmount;
    private String currency;
    private String status;
    private LocalDateTime issueAt;
    private LocalDateTime paidAt;
    private Integer insuranceTotalPay;
    private Integer patientTotalPay;
    private UUID insuranceClaimId;
    private LocalDateTime updateAt;

    // Lồng danh sách DTO của Item
    private List<InvoiceItemResponseDTO> items;
}
