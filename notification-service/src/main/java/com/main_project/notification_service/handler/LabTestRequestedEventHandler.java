package com.main_project.notification_service.handler;

import com.do_an.common.event.LabTestRequestedEvent;
import com.main_project.notification_service.entity.Notification;
import com.main_project.notification_service.repository.NotificationRepository;
import com.main_project.notification_service.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class LabTestRequestedEventHandler {

    private final WebSocketNotificationService webSocketNotificationService;
    private final NotificationRepository notificationRepository;

    @EventHandler
    public void on(LabTestRequestedEvent event) {
        log.info("Received LabTestRequestedEvent for labTest {} appointment={} doctor={}",
                event.getLabTestId(), event.getAppointmentId(), event.getDoctorId());

        try {
            UUID randomUserId = UUID.randomUUID();
            Notification notification = Notification.builder()
                    .userId(randomUserId)
                    .channel("websocket")
                    .templateId("LAB_TEST_REQUESTED")
                    .message(event.getMessage())
                    .status("sent")
                    .retryCount(0)
                    .build();
            notificationRepository.save(notification);
            log.info("Saved single notification to DB with random userId: {}", randomUserId);

            webSocketNotificationService.sendLabTestRequestedNotification(
                    event.getLabTestId(),
                    event.getAppointmentId(),
                    event.getMedicalHistoryId(),
                    event.getDoctorId(),
                    event.getLabTestTypeId(),
                    event.getMessage()
            );

            log.info("Successfully processed lab test requested notification");
        } catch (Exception e) {
            log.error("Failed to process lab test requested notification: {}", e.getMessage(), e);
        }
    }
}