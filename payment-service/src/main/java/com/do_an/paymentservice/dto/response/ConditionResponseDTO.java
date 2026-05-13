package com.do_an.paymentservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConditionResponseDTO {
    private UUID id;
    private UUID medicalHistoryId;
    private Integer toothNumber;
    private String name;
    private String status;
    private String treatment;
    private String surface;
}
