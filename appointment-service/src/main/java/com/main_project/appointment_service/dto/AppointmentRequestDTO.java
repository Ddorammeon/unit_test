package com.main_project.appointment_service.dto;

import com.main_project.appointment_service.enums.AppointmentStatus;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AppointmentRequestDTO {
    private UUID doctorId;
    private UUID patientId;
    private ZonedDateTime appointmentStartTime;
    private ZonedDateTime appointmentEndTime;
    private AppointmentStatus status;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private List<UUID> MedicalServiceIds;
}