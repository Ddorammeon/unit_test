package com.main_project.patient_service.aggregate;

import com.do_an.common.command.RollbackMedicalHistoryCommand;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.UUID;

@Aggregate
@NoArgsConstructor
@Slf4j
public class MedicalHistoryAggregate {

    @AggregateIdentifier
    private UUID medicalHistoryId;
    private UUID clinicalId;
    private UUID appointmentId;
    private UUID patientId;
    private boolean rolledBack;

    public MedicalHistoryAggregate(UUID clinicalId, UUID appointmentId, UUID patientId, UUID medicalHistoryId) {
        log.info("Aggregate: Apply MedicalHistoryCreatedEvent for appointment {}", appointmentId);
        AggregateLifecycle.apply(new MedicalHistoryCreatedEvent(
                clinicalId,
                appointmentId,
                patientId,
                medicalHistoryId
        ));
    }

    public void handle(RollbackMedicalHistoryCommand command) {
        if (this.rolledBack) {
            log.info("Medical history {} already rolled back, skip duplicate command", this.medicalHistoryId);
            return;
        }

        log.warn("Aggregate: Apply MedicalHistoryRolledBackEvent for appointment {} reason={}",
                this.appointmentId, command.getReason());
        AggregateLifecycle.apply(new MedicalHistoryRolledBackEvent(
                this.clinicalId,
                this.appointmentId,
                this.patientId,
                this.medicalHistoryId,
                command.getReason()
        ));
    }

    @EventSourcingHandler
    public void on(MedicalHistoryCreatedEvent event) {
        this.medicalHistoryId = event.getMedicalHistoryId();
        this.clinicalId = event.getClinicalId();
        this.appointmentId = event.getAppointmentId();
        this.patientId = event.getPatientId();
        this.rolledBack = false;
    }

    @EventSourcingHandler
    public void on(MedicalHistoryRolledBackEvent event) {
        this.rolledBack = true;
    }
}
