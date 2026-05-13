package com.main_project.notification_service.handler;


import com.do_an.common.event.InvoicePaidNotificationEvent;
import com.main_project.notification_service.client.InventoryClient;
import com.main_project.notification_service.dto.response.PharmacistResponse;
import com.main_project.notification_service.entity.Notification;
import com.main_project.notification_service.repository.NotificationRepository;
import com.main_project.notification_service.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class InvoicePaidEventHandler {

    private final WebSocketNotificationService webSocketNotificationService;
    private final NotificationRepository notificationRepository;
    private final InventoryClient inventoryClient;

    @EventHandler
    public void on(InvoicePaidNotificationEvent event) {
        log.info("Received InvoicePaidNotificationEvent for invoice {} appointment={}",
                event.getInvoiceId(), event.getAppointmentId());


        try{
            // Lấy danh sách tất cả dược sĩ
            List<PharmacistResponse> pharmacists = inventoryClient.getAllPharmacists();
            log.info("Found {} pharmacists to notify", pharmacists.size());


            for (PharmacistResponse pharmacist: pharmacists){
                try {
                    Notification notification = Notification.builder()
                            .userId(pharmacist.getUserId())
                            .channel("websocket")
                            .templateId("INVOICE_PAID")
                            .message(event.getMessage())
                            .status("sent")
                            .retryCount(0)
                            .build();
                    notificationRepository.save(notification);


                }
                catch (Exception e){
                    log.error("Failed to save notification for pharmacist {}: {}",
                            pharmacist.getUserId(), e.getMessage());
                }
            }

            webSocketNotificationService.sendInvoicePaidNotification(
                    event.getInvoiceId(),
                    event.getAppointmentId(),
                    event.getMessage()
            );
            log.info("Successfully processed invoice paid notification for {} pharmacists", pharmacists.size());
        }
        catch (Exception e){
            log.error("Failed to send websocket notification for invoice paid {}",
                    event.getInvoiceId(), e);
        }

    }
}
