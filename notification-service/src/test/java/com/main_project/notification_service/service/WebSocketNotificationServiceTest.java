package com.main_project.notification_service.service;

import com.main_project.notification_service.dto.AppointmentCreatedNotificationMessage;
import com.main_project.notification_service.dto.InvoicePaidNotificationMessage;
import com.main_project.notification_service.dto.LabTestCompletedNotificationMessage;
import com.main_project.notification_service.dto.LabTestRequestedNotificationMessage;
import com.main_project.notification_service.dto.PrescriptionDispensedNotificationMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebSocketNotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketNotificationService webSocketNotificationService;

    @Test
    @DisplayName("WS-SRV-UT-001 - Send appointment created notification to common topic")
    void sendAppointmentCreatedNotificationShouldPublishExpectedPayload() {
        // Note: WS-SRV-UT-001 | Objective: publish appointment-created payload with the expected topic and identifiers.
        UUID appointmentId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        ZonedDateTime start = fixedTime();
        ZonedDateTime end = start.plusMinutes(30);
        ArgumentCaptor<AppointmentCreatedNotificationMessage> captor = ArgumentCaptor.forClass(AppointmentCreatedNotificationMessage.class);

        webSocketNotificationService.sendAppointmentCreatedNotification(appointmentId, patientId, doctorId, start, end, "created");

        verify(messagingTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("/topic/appointment-created"), captor.capture());
        assertThat(captor.getValue().getAppointmentId()).isEqualTo(appointmentId.toString());
        assertThat(captor.getValue().getPatientId()).isEqualTo(patientId.toString());
        assertThat(captor.getValue().getDoctorId()).isEqualTo(doctorId.toString());
    }

    @Test
    @DisplayName("WS-SRV-UT-002 - Send invoice paid notification to pharmacist topic")
    void sendInvoicePaidNotificationShouldPublishExpectedPayload() {
        // Note: WS-SRV-UT-002 | Objective: publish invoice-paid payload to the shared pharmacist topic.
        UUID invoiceId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        ArgumentCaptor<InvoicePaidNotificationMessage> captor = ArgumentCaptor.forClass(InvoicePaidNotificationMessage.class);

        webSocketNotificationService.sendInvoicePaidNotification(invoiceId, appointmentId, "paid");

        verify(messagingTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("/topic/invoice-paid"), captor.capture());
        assertThat(captor.getValue().getInvoiceId()).isEqualTo(invoiceId.toString());
        assertThat(captor.getValue().getAppointmentId()).isEqualTo(appointmentId.toString());
    }

    @Test
    @DisplayName("WS-SRV-UT-003 - Send lab test completed notification to doctor topic")
    void sendLabTestCompletedNotificationShouldPublishExpectedPayload() {
        // Note: WS-SRV-UT-003 | Objective: publish lab-test-completed payload to the doctor-specific websocket topic.
        UUID doctorId = UUID.randomUUID();
        UUID labTestId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        ArgumentCaptor<LabTestCompletedNotificationMessage> captor = ArgumentCaptor.forClass(LabTestCompletedNotificationMessage.class);

        webSocketNotificationService.sendLabTestCompletedNotification(doctorId, labTestId, appointmentId, "done");

        verify(messagingTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("/topic/notifications/" + doctorId), captor.capture());
        assertThat(captor.getValue().getLabTestId()).isEqualTo(labTestId.toString());
    }

    @Test
    @DisplayName("WS-SRV-UT-004 - Send prescription dispensed notification to shared topic")
    void sendPrescriptionDispensedNotificationShouldPublishExpectedPayload() {
        // Note: WS-SRV-UT-004 | Objective: publish prescription-dispensed payload to the receptionist shared topic.
        UUID dispenseOrderId = UUID.randomUUID();
        UUID prescriptionId = UUID.randomUUID();
        UUID medicalHistoryId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        UUID pharmacistId = UUID.randomUUID();
        ArgumentCaptor<PrescriptionDispensedNotificationMessage> captor = ArgumentCaptor.forClass(PrescriptionDispensedNotificationMessage.class);

        webSocketNotificationService.sendPrescriptionDispensedNotification(dispenseOrderId, prescriptionId, medicalHistoryId, appointmentId, pharmacistId, "Pharm A", "dispensed");

        verify(messagingTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("/topic/prescription-dispensed"), captor.capture());
        assertThat(captor.getValue().getDispenseOrderId()).isEqualTo(dispenseOrderId.toString());
        assertThat(captor.getValue().getPharmacistName()).isEqualTo("Pharm A");
    }

    @Test
    @DisplayName("WS-SRV-UT-005 - Send appointment rollback notification with appointment context")
    void sendAppointmentRollbackNotificationShouldKeepAppointmentIdInPayload() {
        // Note: WS-SRV-UT-005 | Objective: publish appointment rollback payload that still carries the affected appointment id.
        UUID appointmentId = UUID.randomUUID();
        ArgumentCaptor<InvoicePaidNotificationMessage> captor = ArgumentCaptor.forClass(InvoicePaidNotificationMessage.class);

        webSocketNotificationService.sendAppointmentRollbackNotification(appointmentId, "rollback");

        verify(messagingTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("/topic/appointment-rollback/" + appointmentId), captor.capture());
        assertThat(captor.getValue().getAppointmentId()).isEqualTo(appointmentId.toString());
        assertThat(captor.getValue().getType()).isEqualTo("APPOINTMENT_ROLLBACK");
    }

    @Test
    @DisplayName("WS-SRV-UT-006 - Send lab test requested notification to shared topic")
    void sendLabTestRequestedNotificationShouldPublishExpectedPayload() {
        // Note: WS-SRV-UT-006 | Objective: publish lab-test-requested payload to the shared lab request topic.
        UUID labTestId = UUID.randomUUID();
        UUID appointmentId = UUID.randomUUID();
        UUID medicalHistoryId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID labTestTypeId = UUID.randomUUID();
        ArgumentCaptor<LabTestRequestedNotificationMessage> captor = ArgumentCaptor.forClass(LabTestRequestedNotificationMessage.class);

        webSocketNotificationService.sendLabTestRequestedNotification(labTestId, appointmentId, medicalHistoryId, doctorId, labTestTypeId, "request");

        verify(messagingTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("/topic/request-labtest"), captor.capture());
        assertThat(captor.getValue().getDoctorId()).isEqualTo(doctorId.toString());
    }

    @Test
    @DisplayName("WS-SRV-UT-007 - Invoice paid should use default message when null")
    void sendInvoicePaidNotificationShouldUseDefaultMessageWhenMessageIsNull() {
        // Note: WS-SRV-UT-007 | Objective: populate the built-in default message when invoice-paid message input is null.
        UUID invoiceId = UUID.randomUUID();
        ArgumentCaptor<InvoicePaidNotificationMessage> captor = ArgumentCaptor.forClass(InvoicePaidNotificationMessage.class);

        webSocketNotificationService.sendInvoicePaidNotification(invoiceId, null, null);

        verify(messagingTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("/topic/invoice-paid"), captor.capture());
        assertThat(captor.getValue().getMessage()).contains("thanh toán");
    }

    @Test
    @DisplayName("WS-SRV-UT-008 - Appointment created should use default message when null")
    void sendAppointmentCreatedNotificationShouldUseDefaultMessageWhenMessageIsNull() {
        // Note: WS-SRV-UT-008 | Objective: populate the built-in default message when appointment-created message input is null.
        UUID appointmentId = UUID.randomUUID();
        ArgumentCaptor<AppointmentCreatedNotificationMessage> captor = ArgumentCaptor.forClass(AppointmentCreatedNotificationMessage.class);

        webSocketNotificationService.sendAppointmentCreatedNotification(appointmentId, null, null, null, null, null);

        verify(messagingTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("/topic/appointment-created"), captor.capture());
        assertThat(captor.getValue().getMessage()).contains("Lịch hẹn mới");
    }

    private ZonedDateTime fixedTime() {
        return ZonedDateTime.of(2026, 5, 13, 8, 0, 0, 0, ZoneOffset.UTC);
    }
}
