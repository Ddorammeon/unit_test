package com.main_project.clinical_service.saga;

import com.do_an.common.command.CreateInvoiceCommand;
import com.do_an.common.command.CreateMedicalHistoryCommand;
import com.do_an.common.command.RollbackAppointmentCommand;
import com.do_an.common.command.RollbackMedicalHistoryCommand;
import com.do_an.common.event.AppointmentRolledBackEvent;
import com.do_an.common.event.AppointmentStartedEvent;
import com.do_an.common.event.InvoicePersistenceFailedEvent;
import com.do_an.common.event.InvoicePersistedEvent;
import com.do_an.common.event.MedicalHistoryPersistenceFailedEvent;
import com.do_an.common.event.MedicalHistoryPersistedEvent;
import com.do_an.common.event.MedicalHistoryRollbackCompletedEvent;
import com.do_an.common.model.MedicalServiceDTO;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultationSagaTest {

    @Mock
    private CommandGateway commandGateway;

    private ConsultationSaga saga;

    private final UUID clinicalId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID appointmentId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID patientId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private final UUID doctorId = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private final UUID medicalHistoryId = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private final UUID invoiceId = UUID.fromString("66666666-6666-6666-6666-666666666666");

    @BeforeEach
    void setUp() {
        saga = new ConsultationSaga();
        ReflectionTestUtils.setField(saga, "commandGateway", commandGateway);
        when(commandGateway.send(any())).thenReturn(CompletableFuture.completedFuture("ok"));
    }

    @Test
    @DisplayName("CLN-SRV-UT-001 - AppointmentStarted should store patient id in saga state")
    void appointmentStartedShouldStorePatientId() {
        invokeStart(startEvent(defaultServices()));
        assertEquals(patientId, getField("patientId"));
    }

    @Test
    @DisplayName("CLN-SRV-UT-002 - AppointmentStarted should store appointment id in saga state")
    void appointmentStartedShouldStoreAppointmentId() {
        invokeStart(startEvent(defaultServices()));
        assertEquals(appointmentId, getField("appointmentId"));
    }

    @Test
    @DisplayName("CLN-SRV-UT-003 - AppointmentStarted should store doctor id in saga state")
    void appointmentStartedShouldStoreDoctorId() {
        invokeStart(startEvent(defaultServices()));
        assertEquals(doctorId, getField("doctorId"));
    }

    @Test
    @DisplayName("CLN-SRV-UT-004 - AppointmentStarted should copy medical services into saga state")
    void appointmentStartedShouldCopyMedicalServices() {
        List<MedicalServiceDTO> services = defaultServices();
        invokeStart(startEvent(services));
        assertEquals(services, getField("medicalServices"));
    }

    @Test
    @DisplayName("CLN-SRV-UT-005 - AppointmentStarted should replace null medical services with empty list")
    void appointmentStartedShouldReplaceNullMedicalServicesWithEmptyList() {
        invokeStart(startEvent(null));
        assertTrue(((List<?>) getField("medicalServices")).isEmpty());
    }

    @Test
    @DisplayName("CLN-SRV-UT-006 - AppointmentStarted should generate medical history id")
    void appointmentStartedShouldGenerateMedicalHistoryId() {
        invokeStart(startEvent(defaultServices()));
        assertNotNull(getField("medicalHistoryId"));
    }

    @Test
    @DisplayName("CLN-SRV-UT-007 - AppointmentStarted should generate invoice id")
    void appointmentStartedShouldGenerateInvoiceId() {
        invokeStart(startEvent(defaultServices()));
        assertNotNull(getField("invoiceId"));
    }

    @Test
    @DisplayName("CLN-SRV-UT-008 - AppointmentStarted should set saga status to MedicalHistoryPending")
    void appointmentStartedShouldSetMedicalHistoryPendingStatus() {
        invokeStart(startEvent(defaultServices()));
        assertEquals("MedicalHistoryPending", getField("sagaStatus"));
    }

    @Test
    @DisplayName("CLN-SRV-UT-009 - AppointmentStarted should reject null patient id")
    void appointmentStartedShouldRejectNullPatientId() {
        assertThrows(IllegalArgumentException.class, () -> invokeStart(new AppointmentStartedEvent(clinicalId, appointmentId, null, doctorId, defaultServices())));
    }

    @Test
    @DisplayName("CLN-SRV-UT-010 - AppointmentStarted should reject empty medical services list")
    void appointmentStartedShouldRejectEmptyMedicalServicesList() {
        assertThrows(IllegalArgumentException.class, () -> invokeStart(startEvent(List.of())));
    }

    @Test
    @DisplayName("CLN-SRV-UT-011 - AppointmentStarted should dispatch CreateMedicalHistoryCommand with clinical id")
    void appointmentStartedShouldDispatchCreateMedicalHistoryCommandWithClinicalId() {
        invokeStart(startEvent(defaultServices()));
        ArgumentCaptor<CreateMedicalHistoryCommand> captor = ArgumentCaptor.forClass(CreateMedicalHistoryCommand.class);
        verify(commandGateway).send(captor.capture());
        assertEquals(clinicalId, captor.getValue().getClinicalId());
    }

    @Test
    @DisplayName("CLN-SRV-UT-012 - AppointmentStarted should dispatch CreateMedicalHistoryCommand with appointment and patient ids")
    void appointmentStartedShouldDispatchCreateMedicalHistoryCommandWithAppointmentAndPatientIds() {
        invokeStart(startEvent(defaultServices()));
        ArgumentCaptor<CreateMedicalHistoryCommand> captor = ArgumentCaptor.forClass(CreateMedicalHistoryCommand.class);
        verify(commandGateway).send(captor.capture());
        assertAll(
                () -> assertEquals(appointmentId, captor.getValue().getAppointmentId()),
                () -> assertEquals(patientId, captor.getValue().getPatientId()),
                () -> assertEquals(getField("medicalHistoryId"), captor.getValue().getMedicalHistoryId())
        );
    }

    @Test
    @DisplayName("CLN-SRV-UT-013 - AppointmentStarted should associate saga with appointment and clinical ids")
    void appointmentStartedShouldAssociateSagaWithAppointmentAndClinicalIds() {
        try (MockedStatic<SagaLifecycle> sagaLifecycle = mockStatic(SagaLifecycle.class)) {
            saga.on(startEvent(defaultServices()));
            sagaLifecycle.verify(() -> SagaLifecycle.associateWith("appointmentId", appointmentId.toString()));
            sagaLifecycle.verify(() -> SagaLifecycle.associateWith("clinicalId", clinicalId.toString()));
        }
    }

    @Test
    @DisplayName("CLN-SRV-UT-014 - MedicalHistoryPersisted should set saga status to InvoicePending")
    void medicalHistoryPersistedShouldSetInvoicePendingStatus() {
        startSagaWithDefaultEvent();
        saga.on(new MedicalHistoryPersistedEvent(clinicalId, appointmentId, patientId, medicalHistoryId));
        assertEquals("InvoicePending", getField("sagaStatus"));
    }

    @Test
    @DisplayName("CLN-SRV-UT-015 - MedicalHistoryPersisted should update medical history id from event")
    void medicalHistoryPersistedShouldUpdateMedicalHistoryIdFromEvent() {
        startSagaWithDefaultEvent();
        saga.on(new MedicalHistoryPersistedEvent(clinicalId, appointmentId, patientId, medicalHistoryId));
        assertEquals(medicalHistoryId, getField("medicalHistoryId"));
    }

    @Test
    @DisplayName("CLN-SRV-UT-016 - MedicalHistoryPersisted should dispatch CreateInvoiceCommand with generated invoice id")
    void medicalHistoryPersistedShouldDispatchCreateInvoiceCommandWithGeneratedInvoiceId() {
        startSagaWithDefaultEvent();
        UUID generatedInvoiceId = getField("invoiceId");
        saga.on(new MedicalHistoryPersistedEvent(clinicalId, appointmentId, patientId, medicalHistoryId));
        ArgumentCaptor<CreateInvoiceCommand> captor = ArgumentCaptor.forClass(CreateInvoiceCommand.class);
        verify(commandGateway, times(1)).send(captor.capture());
        assertEquals(generatedInvoiceId, captor.getValue().getInvoiceId());
    }

    @Test
    @DisplayName("CLN-SRV-UT-017 - MedicalHistoryPersisted should dispatch CreateInvoiceCommand with appointment patient and doctor ids")
    void medicalHistoryPersistedShouldDispatchCreateInvoiceCommandWithIds() {
        startSagaWithDefaultEvent();
        saga.on(new MedicalHistoryPersistedEvent(clinicalId, appointmentId, patientId, medicalHistoryId));
        ArgumentCaptor<CreateInvoiceCommand> captor = ArgumentCaptor.forClass(CreateInvoiceCommand.class);
        verify(commandGateway, times(1)).send(captor.capture());
        CreateInvoiceCommand command = captor.getValue();
        assertAll(
                () -> assertEquals(clinicalId, command.getClinicalId()),
                () -> assertEquals(appointmentId, command.getAppointmentId()),
                () -> assertEquals(patientId, command.getPatientId()),
                () -> assertEquals(medicalHistoryId, command.getMedicalHistoryId()),
                () -> assertEquals(doctorId, command.getDoctorId())
        );
    }

    @Test
    @DisplayName("CLN-SRV-UT-018 - MedicalHistoryPersisted should pass copied medical services to invoice command")
    void medicalHistoryPersistedShouldPassCopiedMedicalServicesToInvoiceCommand() {
        List<MedicalServiceDTO> services = defaultServices();
        invokeStart(startEvent(services));
        saga.on(new MedicalHistoryPersistedEvent(clinicalId, appointmentId, patientId, medicalHistoryId));
        ArgumentCaptor<CreateInvoiceCommand> captor = ArgumentCaptor.forClass(CreateInvoiceCommand.class);
        verify(commandGateway, times(1)).send(captor.capture());
        assertEquals(services, captor.getValue().getMedicalServices());
    }

    @Test
    @DisplayName("CLN-SRV-UT-019 - MedicalHistoryPersisted should reject event before saga start")
    void medicalHistoryPersistedShouldRejectEventBeforeSagaStart() {
        assertThrows(IllegalStateException.class, () -> saga.on(new MedicalHistoryPersistedEvent(clinicalId, appointmentId, patientId, medicalHistoryId)));
    }

    @Test
    @DisplayName("CLN-SRV-UT-020 - MedicalHistoryPersisted should not dispatch invoice when no medical services exist")
    void medicalHistoryPersistedShouldNotDispatchInvoiceWhenNoMedicalServicesExist() {
        invokeStart(startEvent(null));
        saga.on(new MedicalHistoryPersistedEvent(clinicalId, appointmentId, patientId, medicalHistoryId));
        verify(commandGateway, never()).send(any(CreateInvoiceCommand.class));
    }

    @Test
    @DisplayName("CLN-SRV-UT-021 - InvoicePersisted should update invoice id from event")
    void invoicePersistedShouldUpdateInvoiceIdFromEvent() {
        startSagaWithDefaultEvent();
        saga.on(new InvoicePersistedEvent(clinicalId, appointmentId, patientId, medicalHistoryId, invoiceId));
        assertEquals(invoiceId, getField("invoiceId"));
    }

    @Test
    @DisplayName("CLN-SRV-UT-022 - InvoicePersisted should set saga status to ExaminationStarted")
    void invoicePersistedShouldSetExaminationStartedStatus() {
        startSagaWithDefaultEvent();
        saga.on(new InvoicePersistedEvent(clinicalId, appointmentId, patientId, medicalHistoryId, invoiceId));
        assertEquals("ExaminationStarted", getField("sagaStatus"));
    }

    @Test
    @DisplayName("CLN-SRV-UT-023 - MedicalHistoryPersistenceFailed should set saga status to MedicalHistoryFailed")
    void medicalHistoryPersistenceFailedShouldSetStatus() {
        startSagaWithDefaultEvent();
        saga.on(new MedicalHistoryPersistenceFailedEvent(clinicalId, appointmentId, patientId, medicalHistoryId, "db fail"));
        assertEquals("MedicalHistoryFailed", getField("sagaStatus"));
    }

    @Test
    @DisplayName("CLN-SRV-UT-024 - MedicalHistoryPersistenceFailed should dispatch rollback medical history command")
    void medicalHistoryPersistenceFailedShouldDispatchRollbackMedicalHistoryCommand() {
        startSagaWithDefaultEvent();
        saga.on(new MedicalHistoryPersistenceFailedEvent(clinicalId, appointmentId, patientId, medicalHistoryId, "db fail"));
        ArgumentCaptor<RollbackMedicalHistoryCommand> captor = ArgumentCaptor.forClass(RollbackMedicalHistoryCommand.class);
        verify(commandGateway, times(1)).send(captor.capture());
        assertTrue(captor.getValue().getReason().contains("Medical history failed"));
    }

    @Test
    @DisplayName("CLN-SRV-UT-025 - MedicalHistoryPersistenceFailed should mark medical history rollback as triggered")
    void medicalHistoryPersistenceFailedShouldMarkRollbackTriggered() {
        startSagaWithDefaultEvent();
        saga.on(new MedicalHistoryPersistenceFailedEvent(clinicalId, appointmentId, patientId, medicalHistoryId, "db fail"));
        assertTrue(Boolean.TRUE.equals(getField("medicalHistoryRollbackTriggered")));
    }

    @Test
    @DisplayName("CLN-SRV-UT-026 - InvoicePersistenceFailed should set saga status to InvoiceFailed")
    void invoicePersistenceFailedShouldSetStatus() {
        startSagaWithDefaultEvent();
        saga.on(new InvoicePersistenceFailedEvent(clinicalId, appointmentId, patientId, medicalHistoryId, invoiceId, "invoice down"));
        assertEquals("InvoiceFailed", getField("sagaStatus"));
    }

    @Test
    @DisplayName("CLN-SRV-UT-027 - InvoicePersistenceFailed should dispatch rollback medical history command")
    void invoicePersistenceFailedShouldDispatchRollbackMedicalHistoryCommand() {
        startSagaWithDefaultEvent();
        saga.on(new InvoicePersistenceFailedEvent(clinicalId, appointmentId, patientId, medicalHistoryId, invoiceId, "invoice down"));
        ArgumentCaptor<RollbackMedicalHistoryCommand> captor = ArgumentCaptor.forClass(RollbackMedicalHistoryCommand.class);
        verify(commandGateway, times(1)).send(captor.capture());
        assertTrue(captor.getValue().getReason().contains("Invoice failed"));
    }

    @Test
    @DisplayName("CLN-SRV-UT-028 - InvoicePersistenceFailed should mark medical history rollback as triggered")
    void invoicePersistenceFailedShouldMarkRollbackTriggered() {
        startSagaWithDefaultEvent();
        saga.on(new InvoicePersistenceFailedEvent(clinicalId, appointmentId, patientId, medicalHistoryId, invoiceId, "invoice down"));
        assertTrue(Boolean.TRUE.equals(getField("medicalHistoryRollbackTriggered")));
    }

    @Test
    @DisplayName("CLN-SRV-UT-029 - InvoicePersistenceFailed should not dispatch rollback twice when already triggered")
    void invoicePersistenceFailedShouldNotDispatchRollbackTwiceWhenAlreadyTriggered() {
        startSagaWithDefaultEvent();
        setField("medicalHistoryRollbackTriggered", true);
        saga.on(new InvoicePersistenceFailedEvent(clinicalId, appointmentId, patientId, medicalHistoryId, invoiceId, "invoice down"));
        verify(commandGateway, times(1)).send(any(CreateMedicalHistoryCommand.class));
    }

    @Test
    @DisplayName("CLN-SRV-UT-030 - MedicalHistoryPersistenceFailed should not dispatch rollback twice when already triggered")
    void medicalHistoryPersistenceFailedShouldNotDispatchRollbackTwiceWhenAlreadyTriggered() {
        startSagaWithDefaultEvent();
        setField("medicalHistoryRollbackTriggered", true);
        saga.on(new MedicalHistoryPersistenceFailedEvent(clinicalId, appointmentId, patientId, medicalHistoryId, "db fail"));
        verify(commandGateway, times(1)).send(any(CreateMedicalHistoryCommand.class));
    }

    @Test
    @DisplayName("CLN-SRV-UT-031 - MedicalHistoryPersistenceFailed should do nothing when medical history id is missing")
    void medicalHistoryPersistenceFailedShouldDoNothingWhenMedicalHistoryIdMissing() {
        startSagaWithDefaultEvent();
        setField("medicalHistoryId", null);
        saga.on(new MedicalHistoryPersistenceFailedEvent(clinicalId, appointmentId, patientId, medicalHistoryId, "db fail"));
        verify(commandGateway, times(1)).send(any(CreateMedicalHistoryCommand.class));
    }

    @Test
    @DisplayName("CLN-SRV-UT-032 - InvoicePersistenceFailed should trigger appointment rollback directly when medical history id is missing")
    void invoicePersistenceFailedShouldTriggerAppointmentRollbackDirectlyWhenMedicalHistoryIdMissing() {
        startSagaWithDefaultEvent();
        setField("medicalHistoryId", null);
        saga.on(new InvoicePersistenceFailedEvent(clinicalId, appointmentId, patientId, medicalHistoryId, invoiceId, "invoice down"));
        verify(commandGateway).send(any(RollbackAppointmentCommand.class));
    }

    @Test
    @DisplayName("CLN-SRV-UT-033 - MedicalHistoryRollbackCompleted should dispatch rollback appointment command")
    void medicalHistoryRollbackCompletedShouldDispatchRollbackAppointmentCommand() {
        startSagaWithDefaultEvent();
        saga.on(new MedicalHistoryRollbackCompletedEvent(clinicalId, appointmentId, patientId, medicalHistoryId, "rollback ok"));
        ArgumentCaptor<RollbackAppointmentCommand> captor = ArgumentCaptor.forClass(RollbackAppointmentCommand.class);
        verify(commandGateway, times(1)).send(captor.capture());
        assertEquals(appointmentId, captor.getValue().getAppointmentId());
    }

    @Test
    @DisplayName("CLN-SRV-UT-034 - MedicalHistoryRollbackCompleted should forward rollback reason to appointment rollback command")
    void medicalHistoryRollbackCompletedShouldForwardReason() {
        startSagaWithDefaultEvent();
        saga.on(new MedicalHistoryRollbackCompletedEvent(clinicalId, appointmentId, patientId, medicalHistoryId, "rollback ok"));
        ArgumentCaptor<RollbackAppointmentCommand> captor = ArgumentCaptor.forClass(RollbackAppointmentCommand.class);
        verify(commandGateway, times(1)).send(captor.capture());
        assertEquals("rollback ok", captor.getValue().getReason());
    }

    @Test
    @DisplayName("CLN-SRV-UT-035 - MedicalHistoryRollbackCompleted should mark appointment rollback as triggered")
    void medicalHistoryRollbackCompletedShouldMarkAppointmentRollbackTriggered() {
        startSagaWithDefaultEvent();
        saga.on(new MedicalHistoryRollbackCompletedEvent(clinicalId, appointmentId, patientId, medicalHistoryId, "rollback ok"));
        assertTrue(Boolean.TRUE.equals(getField("appointmentRollbackTriggered")));
    }

    @Test
    @DisplayName("CLN-SRV-UT-036 - MedicalHistoryRollbackCompleted should not dispatch appointment rollback twice")
    void medicalHistoryRollbackCompletedShouldNotDispatchAppointmentRollbackTwice() {
        startSagaWithDefaultEvent();
        setField("appointmentRollbackTriggered", true);
        saga.on(new MedicalHistoryRollbackCompletedEvent(clinicalId, appointmentId, patientId, medicalHistoryId, "rollback ok"));
        verify(commandGateway, times(1)).send(any(CreateMedicalHistoryCommand.class));
    }

    @Test
    @DisplayName("CLN-SRV-UT-037 - MedicalHistoryRollbackCompleted should do nothing when appointment id is missing")
    void medicalHistoryRollbackCompletedShouldDoNothingWhenAppointmentIdMissing() {
        startSagaWithDefaultEvent();
        setField("appointmentId", null);
        saga.on(new MedicalHistoryRollbackCompletedEvent(clinicalId, appointmentId, patientId, medicalHistoryId, "rollback ok"));
        verify(commandGateway, times(1)).send(any(CreateMedicalHistoryCommand.class));
    }

    @Test
    @DisplayName("CLN-SRV-UT-038 - AppointmentRolledBack should set saga status to RolledBack")
    void appointmentRolledBackShouldSetStatus() {
        startSagaWithDefaultEvent();
        saga.on(new AppointmentRolledBackEvent(clinicalId, appointmentId, "rollback ok"));
        assertEquals("RolledBack", getField("sagaStatus"));
    }

    @Test
    @DisplayName("CLN-SRV-UT-039 - AppointmentRolledBack should reset rollback trigger flags")
    void appointmentRolledBackShouldResetRollbackTriggerFlags() {
        startSagaWithDefaultEvent();
        setField("medicalHistoryRollbackTriggered", true);
        setField("appointmentRollbackTriggered", true);
        saga.on(new AppointmentRolledBackEvent(clinicalId, appointmentId, "rollback ok"));
        assertAll(
                () -> assertEquals(false, getField("medicalHistoryRollbackTriggered")),
                () -> assertEquals(false, getField("appointmentRollbackTriggered"))
        );
    }

    @Test
    @DisplayName("CLN-SRV-UT-040 - AppointmentStarted should keep state even when medical history dispatch future fails")
    void appointmentStartedShouldKeepStateWhenMedicalHistoryDispatchFutureFails() {
        when(commandGateway.send(any())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("dispatch fail")));
        invokeStart(startEvent(defaultServices()));
        assertEquals("MedicalHistoryPending", getField("sagaStatus"));
    }

    @Test
    @DisplayName("CLN-SRV-UT-041 - MedicalHistoryPersisted should keep invoice pending state when invoice dispatch future fails")
    void medicalHistoryPersistedShouldKeepInvoicePendingWhenDispatchFutureFails() {
        when(commandGateway.send(any())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("dispatch fail")));
        invokeStart(startEvent(defaultServices()));
        saga.on(new MedicalHistoryPersistedEvent(clinicalId, appointmentId, patientId, medicalHistoryId));
        assertEquals("InvoicePending", getField("sagaStatus"));
    }

    @Test
    @DisplayName("CLN-SRV-UT-042 - MedicalHistoryPersistenceFailed should keep rollback triggered state when rollback dispatch future fails")
    void medicalHistoryPersistenceFailedShouldKeepRollbackTriggeredWhenDispatchFutureFails() {
        when(commandGateway.send(any())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("dispatch fail")));
        invokeStart(startEvent(defaultServices()));
        saga.on(new MedicalHistoryPersistenceFailedEvent(clinicalId, appointmentId, patientId, medicalHistoryId, "db fail"));
        assertTrue(Boolean.TRUE.equals(getField("medicalHistoryRollbackTriggered")));
    }

    @Test
    @DisplayName("CLN-SRV-UT-043 - MedicalHistoryRollbackCompleted should keep appointment rollback triggered when dispatch future fails")
    void medicalHistoryRollbackCompletedShouldKeepAppointmentRollbackTriggeredWhenDispatchFutureFails() {
        when(commandGateway.send(any())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("dispatch fail")));
        invokeStart(startEvent(defaultServices()));
        saga.on(new MedicalHistoryRollbackCompletedEvent(clinicalId, appointmentId, patientId, medicalHistoryId, "rollback ok"));
        assertTrue(Boolean.TRUE.equals(getField("appointmentRollbackTriggered")));
    }

    @Test
    @DisplayName("CLN-SRV-UT-044 - AppointmentStarted should preserve clinical id in saga state")
    void appointmentStartedShouldPreserveClinicalId() {
        invokeStart(startEvent(defaultServices()));
        assertEquals(clinicalId, getField("clinicalId"));
    }

    @Test
    @DisplayName("CLN-SRV-UT-045 - InvoicePersisted should overwrite generated invoice id with persisted id")
    void invoicePersistedShouldOverwriteGeneratedInvoiceId() {
        startSagaWithDefaultEvent();
        UUID generatedInvoiceId = getField("invoiceId");
        saga.on(new InvoicePersistedEvent(clinicalId, appointmentId, patientId, medicalHistoryId, invoiceId));
        assertNotEquals(generatedInvoiceId, getField("invoiceId"));
    }

    @Test
    @DisplayName("CLN-SRV-UT-046 - AppointmentStarted should keep a defensive copy of medical services list")
    void appointmentStartedShouldKeepDefensiveCopyOfMedicalServicesList() {
        List<MedicalServiceDTO> services = new ArrayList<>(defaultServices());
        invokeStart(startEvent(services));
        services.add(service("X-Ray"));
        assertEquals(2, ((List<?>) getField("medicalServices")).size());
    }

    @Test
    @DisplayName("CLN-SRV-UT-047 - MedicalHistoryPersisted should use clinical id as invoice command target aggregate")
    void medicalHistoryPersistedShouldUseClinicalIdAsInvoiceCommandTarget() {
        startSagaWithDefaultEvent();
        saga.on(new MedicalHistoryPersistedEvent(clinicalId, appointmentId, patientId, medicalHistoryId));
        ArgumentCaptor<CreateInvoiceCommand> captor = ArgumentCaptor.forClass(CreateInvoiceCommand.class);
        verify(commandGateway, times(1)).send(captor.capture());
        assertEquals(clinicalId, captor.getValue().getClinicalId());
    }

    @Test
    @DisplayName("CLN-SRV-UT-048 - MedicalHistoryRollbackCompleted should use appointment id as rollback command target")
    void medicalHistoryRollbackCompletedShouldUseAppointmentIdAsRollbackCommandTarget() {
        startSagaWithDefaultEvent();
        saga.on(new MedicalHistoryRollbackCompletedEvent(clinicalId, appointmentId, patientId, medicalHistoryId, "rollback ok"));
        ArgumentCaptor<RollbackAppointmentCommand> captor = ArgumentCaptor.forClass(RollbackAppointmentCommand.class);
        verify(commandGateway, times(1)).send(captor.capture());
        assertEquals(appointmentId, captor.getValue().getAppointmentId());
    }

    private void startSagaWithDefaultEvent() {
        invokeStart(startEvent(defaultServices()));
    }

    private void invokeStart(AppointmentStartedEvent event) {
        try (MockedStatic<SagaLifecycle> ignored = mockStatic(SagaLifecycle.class)) {
            saga.on(event);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getField(String fieldName) {
        return (T) ReflectionTestUtils.getField(saga, fieldName);
    }

    private void setField(String fieldName, Object value) {
        ReflectionTestUtils.setField(saga, fieldName, value);
    }

    private AppointmentStartedEvent startEvent(List<MedicalServiceDTO> services) {
        return new AppointmentStartedEvent(clinicalId, appointmentId, patientId, doctorId, services);
    }

    private List<MedicalServiceDTO> defaultServices() {
        return new ArrayList<>(List.of(service("Cleaning"), service("Whitening")));
    }

    private MedicalServiceDTO service(String name) {
        return MedicalServiceDTO.builder()
                .id(UUID.randomUUID())
                .serviceName(name)
                .serviceType("Dental")
                .serviceTime(30)
                .price(100_000f)
                .description(name)
                .build();
    }
}
