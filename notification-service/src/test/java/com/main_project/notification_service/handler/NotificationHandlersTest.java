package com.main_project.notification_service.handler;

import com.do_an.common.event.AppointmentCreatedEvent;
import com.do_an.common.event.AppointmentRolledBackEvent;
import com.do_an.common.event.InvoicePaidNotificationEvent;
import com.do_an.common.event.LabTestRequestedEvent;
import com.do_an.common.event.PrescriptionDispensedEvent;
import com.main_project.notification_service.client.InventoryClient;
import com.main_project.notification_service.client.InvoiceClient;
import com.main_project.notification_service.dto.response.InvoiceResponseDTO;
import com.main_project.notification_service.dto.response.PharmacistResponse;
import com.main_project.notification_service.entity.Notification;
import com.main_project.notification_service.event.LabTestCompletedEventHandler;
import com.main_project.notification_service.repository.NotificationRepository;
import com.main_project.notification_service.service.WebSocketNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationHandlersTest {

    @Mock
    private WebSocketNotificationService webSocketNotificationService;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private InventoryClient inventoryClient;
    @Mock
    private InvoiceClient invoiceClient;

    @InjectMocks
    private AppointmentCreatedEventHandler appointmentCreatedEventHandler;
    @InjectMocks
    private AppointmentRollbackEventHandler appointmentRollbackEventHandler;
    @InjectMocks
    private InvoicePaidEventHandler invoicePaidEventHandler;
    @InjectMocks
    private LabTestRequestedEventHandler labTestRequestedEventHandler;
    @InjectMocks
    private PrescriptionDispensedEventHandler prescriptionDispensedEventHandler;

    @Test
    @DisplayName("NTF-HDL-UT-001 - Save and push appointment created notification")
    void appointmentCreatedHandlerShouldSaveNotificationForPatientAndPushSocket() {
        // Note: NTF-HDL-UT-001 | Objective: store appointment-created notification for the patient and push websocket message.
        AppointmentCreatedEvent event = new AppointmentCreatedEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), fixedTime(), fixedTime().plusMinutes(30), "created");
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        appointmentCreatedEventHandler.on(event);

        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(event.getPatientId());
        verify(webSocketNotificationService).sendAppointmentCreatedNotification(event.getAppointmentId(), event.getPatientId(), event.getDoctorId(), event.getAppointmentStartTime(), event.getAppointmentEndTime(), event.getMessage());
    }

    @Test
    @DisplayName("NTF-HDL-UT-002 - Push rollback notification")
    void appointmentRollbackHandlerShouldForwardRollbackToWebsocketService() {
        // Note: NTF-HDL-UT-002 | Objective: forward appointment rollback event to websocket notification service.
        AppointmentRolledBackEvent event = new AppointmentRolledBackEvent(UUID.randomUUID(), UUID.randomUUID(), "rollback");

        appointmentRollbackEventHandler.on(event);

        verify(webSocketNotificationService).sendAppointmentRollbackNotification(event.getAppointmentId(), event.getReason());
    }

    @Test
    @DisplayName("NTF-HDL-UT-003 - Save invoice paid notifications for all pharmacists")
    void invoicePaidHandlerShouldSaveNotificationForEveryPharmacistAndPushSocket() {
        // Note: NTF-HDL-UT-003 | Objective: create one DB notification per pharmacist and send a shared websocket invoice-paid message.
        InvoicePaidNotificationEvent event = new InvoicePaidNotificationEvent(UUID.randomUUID(), UUID.randomUUID(), "paid");
        PharmacistResponse p1 = new PharmacistResponse();
        p1.setUserId(UUID.randomUUID());
        PharmacistResponse p2 = new PharmacistResponse();
        p2.setUserId(UUID.randomUUID());
        when(inventoryClient.getAllPharmacists()).thenReturn(List.of(p1, p2));

        invoicePaidEventHandler.on(event);

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(webSocketNotificationService).sendInvoicePaidNotification(event.getInvoiceId(), event.getAppointmentId(), event.getMessage());
    }

    @Test
    @DisplayName("NTF-HDL-UT-004 - Lab test requested should save notification for doctor")
    void labTestRequestedHandlerShouldSaveNotificationForDoctor() {
        // Note: NTF-HDL-UT-004 | Objective: save lab-test-requested notification for the responsible doctor rather than a random user.
        LabTestRequestedEvent event = new LabTestRequestedEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 100, "note", "requested");
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        labTestRequestedEventHandler.on(event);

        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(event.getDoctorId());
    }

    @Test
    @DisplayName("NTF-HDL-UT-005 - Prescription dispensed should save receptionist notification when invoice has receptionist")
    void prescriptionDispensedHandlerShouldSaveNotificationForReceptionistWhenAvailable() {
        // Note: NTF-HDL-UT-005 | Objective: save notification for receptionist resolved from invoice data and push websocket message.
        PrescriptionDispensedEvent event = new PrescriptionDispensedEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Pharm A", "dispensed");
        InvoiceResponseDTO invoice = new InvoiceResponseDTO();
        UUID receptionistId = UUID.randomUUID();
        invoice.setReceptionistId(receptionistId.toString());
        when(invoiceClient.getInvoicesByAppointmentId(event.getAppointmentId())).thenReturn(List.of(invoice));
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        prescriptionDispensedEventHandler.on(event);

        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(receptionistId);
        verify(webSocketNotificationService).sendPrescriptionDispensedNotification(event.getDispenseOrderId(), event.getPrescriptionId(), event.getMedicalHistoryId(), event.getAppointmentId(), event.getPharmacistId(), event.getPharmacistName(), event.getMessage());
    }

    @Test
    @DisplayName("NTF-HDL-UT-006 - Prescription dispensed should not save notification when receptionist is missing")
    void prescriptionDispensedHandlerShouldNotSaveWhenReceptionistIsMissing() {
        // Note: NTF-HDL-UT-006 | Objective: skip DB notification persistence when no receptionist can be resolved from invoice data.
        PrescriptionDispensedEvent event = new PrescriptionDispensedEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Pharm A", "dispensed");
        when(invoiceClient.getInvoicesByAppointmentId(event.getAppointmentId())).thenReturn(List.of());

        prescriptionDispensedEventHandler.on(event);

        verify(notificationRepository, never()).save(any(Notification.class));
        verify(webSocketNotificationService).sendPrescriptionDispensedNotification(event.getDispenseOrderId(), event.getPrescriptionId(), event.getMedicalHistoryId(), event.getAppointmentId(), event.getPharmacistId(), event.getPharmacistName(), event.getMessage());
    }

    @Test
    @DisplayName("NTF-HDL-UT-007 - Appointment created should still persist when websocket send fails")
    void appointmentCreatedHandlerShouldKeepSavedNotificationWhenWebsocketFails() {
        // Note: NTF-HDL-UT-007 | Objective: keep the DB notification saved even if websocket push throws afterward.
        AppointmentCreatedEvent event = new AppointmentCreatedEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), fixedTime(), fixedTime().plusMinutes(30), "created");
        doThrow(new RuntimeException("socket fail")).when(webSocketNotificationService).sendAppointmentCreatedNotification(any(), any(), any(), any(), any(), any());

        appointmentCreatedEventHandler.on(event);

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("NTF-HDL-UT-008 - Appointment created websocket failure should create failed audit notification")
    void appointmentCreatedHandlerShouldCreateFailedAuditNotificationWhenWebsocketFails() {
        // Note: NTF-HDL-UT-008 | Objective: create a failed audit notification when websocket send fails for appointment-created flow.
        AppointmentCreatedEvent event = new AppointmentCreatedEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), fixedTime(), fixedTime().plusMinutes(30), "created");
        doThrow(new RuntimeException("socket fail")).when(webSocketNotificationService).sendAppointmentCreatedNotification(any(), any(), any(), any(), any(), any());

        appointmentCreatedEventHandler.on(event);

        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    @DisplayName("NTF-HDL-UT-009 - Invoice paid should still push websocket when pharmacist lookup fails")
    void invoicePaidHandlerShouldStillPushWebsocketWhenPharmacistLookupFails() {
        // Note: NTF-HDL-UT-009 | Objective: still send shared websocket notification even when pharmacist lookup fails.
        InvoicePaidNotificationEvent event = new InvoicePaidNotificationEvent(UUID.randomUUID(), UUID.randomUUID(), "paid");
        when(inventoryClient.getAllPharmacists()).thenThrow(new RuntimeException("inventory down"));

        invoicePaidEventHandler.on(event);

        verify(webSocketNotificationService).sendInvoicePaidNotification(event.getInvoiceId(), event.getAppointmentId(), event.getMessage());
    }

    @Test
    @DisplayName("NTF-HDL-UT-010 - Invoice paid should continue when one pharmacist save fails")
    void invoicePaidHandlerShouldContinueWhenOnePharmacistSaveFails() {
        // Note: NTF-HDL-UT-010 | Objective: continue processing remaining pharmacists and websocket send even if one DB save fails.
        InvoicePaidNotificationEvent event = new InvoicePaidNotificationEvent(UUID.randomUUID(), UUID.randomUUID(), "paid");
        PharmacistResponse p1 = new PharmacistResponse();
        p1.setUserId(UUID.randomUUID());
        PharmacistResponse p2 = new PharmacistResponse();
        p2.setUserId(UUID.randomUUID());
        when(inventoryClient.getAllPharmacists()).thenReturn(List.of(p1, p2));
        when(notificationRepository.save(any(Notification.class)))
                .thenThrow(new RuntimeException("db fail"))
                .thenReturn(Notification.builder().build());

        invoicePaidEventHandler.on(event);

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(webSocketNotificationService).sendInvoicePaidNotification(event.getInvoiceId(), event.getAppointmentId(), event.getMessage());
    }

    @Test
    @DisplayName("NTF-HDL-UT-011 - Prescription dispensed should ignore malformed receptionist id")
    void prescriptionDispensedHandlerShouldIgnoreMalformedReceptionistId() {
        // Note: NTF-HDL-UT-011 | Objective: skip DB save but still send websocket when receptionistId cannot be parsed as UUID.
        PrescriptionDispensedEvent event = new PrescriptionDispensedEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Pharm A", "dispensed");
        InvoiceResponseDTO invoice = new InvoiceResponseDTO();
        invoice.setReceptionistId("not-a-uuid");
        when(invoiceClient.getInvoicesByAppointmentId(event.getAppointmentId())).thenReturn(List.of(invoice));

        prescriptionDispensedEventHandler.on(event);

        verify(notificationRepository, never()).save(any(Notification.class));
        verify(webSocketNotificationService).sendPrescriptionDispensedNotification(event.getDispenseOrderId(), event.getPrescriptionId(), event.getMedicalHistoryId(), event.getAppointmentId(), event.getPharmacistId(), event.getPharmacistName(), event.getMessage());
    }

    private ZonedDateTime fixedTime() {
        return ZonedDateTime.of(2026, 5, 13, 8, 0, 0, 0, ZoneOffset.UTC);
    }
}
