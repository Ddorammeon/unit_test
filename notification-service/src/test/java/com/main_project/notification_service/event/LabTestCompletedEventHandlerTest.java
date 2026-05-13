package com.main_project.notification_service.event;

import com.do_an.common.event.LabTestCompletedEvent;
import com.main_project.notification_service.entity.Notification;
import com.main_project.notification_service.repository.NotificationRepository;
import com.main_project.notification_service.service.WebSocketNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LabTestCompletedEventHandlerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private WebSocketNotificationService webSocketNotificationService;

    @InjectMocks
    private LabTestCompletedEventHandler labTestCompletedEventHandler;

    @Test
    @DisplayName("LAB-CPL-HDL-UT-001 - Save and push lab test completed notification")
    void shouldSaveAndSendNotificationWhenDoctorIdExists() {
        // Note: LAB-CPL-HDL-UT-001 | Objective: save DB notification and push websocket message when doctorId exists.
        LabTestCompletedEvent event = new LabTestCompletedEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 100);
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        doAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(UUID.randomUUID());
            return notification;
        }).when(notificationRepository).save(any(Notification.class));

        labTestCompletedEventHandler.on(event);

        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(event.getDoctorId());
        verify(webSocketNotificationService).sendLabTestCompletedNotification(
                event.getDoctorId(),
                event.getLabTestId(),
                event.getAppointmentId(),
                captor.getValue().getMessage()
        );
    }

    @Test
    @DisplayName("LAB-CPL-HDL-UT-002 - Save failed notification when websocket send fails")
    void shouldSaveFailedNotificationWhenWebsocketFails() {
        // Note: LAB-CPL-HDL-UT-002 | Objective: persist a failed notification audit record when websocket sending throws.
        LabTestCompletedEvent event = new LabTestCompletedEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 100);
        doAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(UUID.randomUUID());
            return notification;
        }).when(notificationRepository).save(any(Notification.class));
        doThrow(new RuntimeException("socket fail")).when(webSocketNotificationService)
                .sendLabTestCompletedNotification(any(), any(), any(), any());

        labTestCompletedEventHandler.on(event);

        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    @DisplayName("LAB-CPL-HDL-UT-003 - Skip processing when doctor id is null")
    void shouldSkipProcessingWhenDoctorIdIsNull() {
        // Note: LAB-CPL-HDL-UT-003 | Objective: skip websocket and repository interactions when doctorId is null.
        LabTestCompletedEvent event = new LabTestCompletedEvent(UUID.randomUUID(), UUID.randomUUID(), null, 100);

        labTestCompletedEventHandler.on(event);

        verify(notificationRepository, never()).save(any(Notification.class));
        verify(webSocketNotificationService, never()).sendLabTestCompletedNotification(any(), any(), any(), any());
    }

    @Test
    @DisplayName("LAB-CPL-HDL-UT-004 - Null doctor id should still create failed audit notification")
    void shouldCreateFailedAuditNotificationWhenDoctorIdIsNull() {
        // Note: LAB-CPL-HDL-UT-004 | Objective: create a failed audit notification even when doctorId is null so the event is traceable.
        LabTestCompletedEvent event = new LabTestCompletedEvent(UUID.randomUUID(), UUID.randomUUID(), null, 100);

        labTestCompletedEventHandler.on(event);

        verify(notificationRepository).save(any(Notification.class));
    }
}
