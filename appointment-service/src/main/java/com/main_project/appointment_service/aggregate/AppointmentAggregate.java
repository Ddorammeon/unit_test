package com.main_project.appointment_service.aggregate;

import com.do_an.common.command.RollbackAppointmentCommand;
import com.do_an.common.command.StartAppointmentCommand;
import com.do_an.common.event.AppointmentRolledBackEvent;
import com.do_an.common.event.AppointmentStartedEvent;
import com.do_an.common.model.MedicalServiceDTO;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Aggregate
@NoArgsConstructor
@Slf4j
public class AppointmentAggregate {

    @AggregateIdentifier
    private UUID appointmentId;

    private String status;
    private UUID clinicalId;

    public AppointmentAggregate(CheckInAppointmentCommand command) {
        log.info("Apply CheckInAppointmentEvent {}", command.getAppointmentId());
        AggregateLifecycle.apply(new CheckInAppointmentEvent(
                command.getAppointmentId()
        ));
    }

    @EventSourcingHandler
    public void on(CheckInAppointmentEvent event) {
        this.appointmentId = event.getAppointmentId();
        this.status = "CHECKED";
    }

    public void handle(StartAppointmentCommand command) {

//        if (!"CHECKED".equalsIgnoreCase(this.status)) {
//            throw new IllegalStateException("Cannot start appointment before check-in");
//        }

        log.info("Apply AppointmentStartedEvent {}", command.getAppointmentId());
        AggregateLifecycle.apply(new AppointmentStartedEvent(
                command.getClinicalId(),
                command.getAppointmentId(),
                command.getPatientId(),
                command.getDoctorId(),
                copy(command.getMedicalServices())
        ));
    }

    public void handle(RollbackAppointmentCommand command) {
        if ("ROLLBACK".equalsIgnoreCase(this.status)) {
            log.info("Appointment {} already rolled back, skipping duplicate command", this.appointmentId);
            return;
        }

        log.warn("Apply AppointmentRolledBackEvent {} reason={}", command.getAppointmentId(), command.getReason());
        AggregateLifecycle.apply(new AppointmentRolledBackEvent(
                this.clinicalId,
                command.getAppointmentId(),
                command.getReason()
        ));
    }

    @EventSourcingHandler
    public void on(AppointmentStartedEvent event) {
        this.appointmentId = event.getAppointmentId();
        this.clinicalId = event.getClinicalId();
        this.status = "IN_PROGRESS";
    }

    @EventSourcingHandler
    public void on(AppointmentRolledBackEvent event) {
        this.status = "ROLLBACK";
    }

    private List<MedicalServiceDTO> copy(List<MedicalServiceDTO> s) {
        return s == null ? new ArrayList<>() : new ArrayList<>(s);
    }
}
