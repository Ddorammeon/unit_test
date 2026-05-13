package com.main_project.notification_service.handler;

import com.do_an.common.event.PrescriptionProcessNotificationEvent;
import com.main_project.notification_service.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PrescriptionProcessEventHandler {

    private final SimpMessagingTemplate messagingTemplate;

    // Lắng nghe sự kiện từ Axon Server (được phát ra bởi Saga bên Prescription Service)

    @EventHandler
    public void on(PrescriptionProcessNotificationEvent event) {
        log.info("🔔 Notification Service nhận được Event từ Saga: [{}] - {}", event.getStatus(), event.getMessage());

        // Đẩy xuống WebSocket cho Frontend
        // Client (Frontend) cần subscribe vào topic: /topic/notifications/{doctorId}
        String destination = "/topic/prescription-error/" + event.getDoctorId();

        messagingTemplate.convertAndSend(destination, event);

        log.info("--> Đã đẩy thông báo tới WebSocket topic: {}", destination);
    }
}