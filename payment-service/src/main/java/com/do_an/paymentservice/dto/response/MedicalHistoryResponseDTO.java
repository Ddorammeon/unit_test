package com.do_an.paymentservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalHistoryResponseDTO {
    private UUID id;
    private UUID appointmentId;
    private String symptoms;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private UUID patientId;
    private List<ConditionResponseDTO> conditions;
}
