package com.do_an.invoiceservice.dto.response;

import com.do_an.common.model.MedicalServiceDTO;
import com.do_an.invoiceservice.dto.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class AppointmentDTO {
    private UUID id;
    private UUID doctorId;
    private UUID patientId;
    private ZonedDateTime appointmentStartTime;
    private ZonedDateTime appointmentEndTime;
    private AppointmentStatus status;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private List<MedicalServiceDTO> medicalServices;
}