package com.main_project.insurance_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {

    private UUID id;
    private UUID receptionistId;
    private UUID appointmentId;
    private UUID insuranceClaimId;

    private Integer totalAmount;
    private String currency;
    private String status;


    private Integer insuranceTotalPay;
    private Integer patientTotalPay;

    private LocalDateTime issueAt;
    private LocalDateTime paidAt;
    private LocalDateTime updateAt;

    private Set<InvoiceItemDTO> items;
}
