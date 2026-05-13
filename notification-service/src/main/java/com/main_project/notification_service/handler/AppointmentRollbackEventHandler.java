package com.main_project.notification_service.handler;

import com.do_an.common.event.AppointmentRolledBackEvent;
import com.main_project.notification_service.service.WebSocketNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AppointmentRollbackEventHandler {

    private final WebSocketNotificationService webSocketNotificationService;

    public AppointmentRollbackEventHandler(WebSocketNotificationService webSocketNotificationService) {
        this.webSocketNotificationService = webSocketNotificationService;
    }

    @EventHandler
    public void on(AppointmentRolledBackEvent event) {
        log.info("Received AppointmentRolledBackEvent for appointment {} reason={}", 
                event.getAppointmentId(), event.getReason());
        
        try {
            webSocketNotificationService.sendAppointmentRollbackNotification(
                    event.getAppointmentId(),
                    event.getReason()
            );
        } catch (Exception e) {
            log.error("Failed to send websocket notification for appointment rollback {}", 
                    event.getAppointmentId(), e);
        }
    }
}