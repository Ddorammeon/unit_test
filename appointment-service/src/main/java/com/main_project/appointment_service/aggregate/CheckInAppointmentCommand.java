package com.main_project.appointment_service.aggregate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInAppointmentCommand {
    @TargetAggregateIdentifier
    private UUID appointmentId;
}
