package com.main_project.notification_service.handler;

import com.do_an.common.event.AppointmentCreatedEvent;
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
public class AppointmentCreatedEventHandler {

    private final WebSocketNotificationService webSocketNotificationService;
    private final NotificationRepository notificationRepository;

    @EventHandler
    public void on(AppointmentCreatedEvent event) {
        log.info("Received AppointmentCreatedEvent for appointment {} patient={} doctor={}",
                event.getAppointmentId(), event.getPatientId(), event.getDoctorId());

        try {
            //UUID randomUserId = UUID.randomUUID();
            Notification notification = Notification.builder()
                    //.userId(randomUserId)
                    .userId(event.getPatientId())
                    .channel("websocket")
                    .templateId("APPOINTMENT_CREATED")
                    .message(event.getMessage())
                    .status("sent")
                    .retryCount(0)
                    .build();
            notificationRepository.save(notification);
            log.info(" Saved single notification to DB with userId: {}", event.getPatientId());

            webSocketNotificationService.sendAppointmentCreatedNotification(
                    event.getAppointmentId(),
                    event.getPatientId(),
                    event.getDoctorId(),
                    event.getAppointmentStartTime(),
                    event.getAppointmentEndTime(),
                    event.getMessage()
            );

            log.info(" Successfully processed appointment created notification");
        } catch (Exception e) {
            log.error(" Failed to process appointment created notification: {}", e.getMessage(), e);
        }
    }
}