package com.main_project.appointment_service.aggregate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInAppointmentEvent {
    private UUID appointmentId;
}
