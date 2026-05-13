package com.do_an.invoiceservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateInvoiceRequestDTO {
    private UUID id;
    @NotNull
    private UUID receptionistId;
    @NotNull
    private UUID appointmentId;
    @NotEmpty
    private String currency;

    private Integer insuranceTotalPay;
    private Integer patientTotalPay;
    private UUID insuranceClaimId;


    // Danh sách "ĐẦY ĐỦ" các item mà hóa đơn NÊN CÓ
    @Valid
    @NotEmpty
    @Size(min = 1)
    private List<CreateInvoiceItemRequestDTO> items; // Cập nhật type
}
