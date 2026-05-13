package com.main_project.patient_service.aggregate;

import com.do_an.common.event.MedicalHistoryPersistenceFailedEvent;
import com.do_an.common.event.MedicalHistoryPersistedEvent;
import com.do_an.common.event.MedicalHistoryRollbackCompletedEvent;
import com.main_project.patient_service.entity.MedicalHistory;
import com.main_project.patient_service.entity.Patient;
import com.main_project.patient_service.repository.MedicalHistoryRepository;
import com.main_project.patient_service.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.GenericEventMessage;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MedicalHistoryEventHandler {
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final PatientRepository patientRepository;
    private final EventBus eventBus;


    @EventHandler
    @Transactional
    public void on(MedicalHistoryCreatedEvent event) {
        try {
            Patient patient = patientRepository.findById(event.getPatientId())
                    .orElseThrow(() -> new EntityNotFoundException("Patient not found for user " + event.getPatientId()));

            MedicalHistory medicalHistory = MedicalHistory.builder()
                    .id(event.getMedicalHistoryId())
                    .appointmentId(event.getAppointmentId())
                    .patient(patient)
                    .build();

            medicalHistoryRepository.save(medicalHistory);

            eventBus.publish(GenericEventMessage.asEventMessage(
                    new MedicalHistoryPersistedEvent(
                            event.getClinicalId(),
                            event.getAppointmentId(),
                            event.getPatientId(),
                            event.getMedicalHistoryId()
                    )
            ));

            log.info("MedicalHistory persisted and event published for medicalHistoryId {}", event.getMedicalHistoryId());
        } catch (Exception e) {
            log.error("Failed to persist medical history {}: {}", event.getMedicalHistoryId(), e.getMessage(), e);
            eventBus.publish(GenericEventMessage.asEventMessage(
                    new MedicalHistoryPersistenceFailedEvent(
                            event.getClinicalId(),
                            event.getAppointmentId(),
                            event.getPatientId(),
                            event.getMedicalHistoryId(),
                            e.getMessage()
                    )
            ));
        }
    }

    @EventHandler
    @Transactional
    public void on(MedicalHistoryRolledBackEvent event) {
        try {
            Optional<MedicalHistory> record = medicalHistoryRepository.findById(event.getMedicalHistoryId());
            record.ifPresent(medicalHistoryRepository::delete);

            eventBus.publish(GenericEventMessage.asEventMessage(
                    new MedicalHistoryRollbackCompletedEvent(
                            event.getClinicalId(),
                            event.getAppointmentId(),
                            event.getPatientId(),
                            event.getMedicalHistoryId(),
                            event.getReason()
                    )
            ));
            log.info("Medical history {} rolled back successfully", event.getMedicalHistoryId());
        } catch (Exception e) {
            log.error("Failed to rollback medical history {}: {}", event.getMedicalHistoryId(), e.getMessage(), e);
        }
    }
}