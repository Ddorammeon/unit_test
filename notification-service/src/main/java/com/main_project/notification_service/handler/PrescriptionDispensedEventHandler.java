package com.main_project.notification_service.handler;

import com.do_an.common.event.PrescriptionDispensedEvent;
import com.main_project.notification_service.client.InvoiceClient;
import com.main_project.notification_service.entity.Notification;
import com.main_project.notification_service.repository.NotificationRepository;
import com.main_project.notification_service.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class PrescriptionDispensedEventHandler {

    private final WebSocketNotificationService webSocketNotificationService;
    private final NotificationRepository notificationRepository;
    private final InvoiceClient invoiceClient;

    @EventHandler
    public void on(PrescriptionDispensedEvent event) {
        log.info("Received PrescriptionDispensedEvent for dispenseOrder {} pharmacist={}",
                event.getDispenseOrderId(), event.getPharmacistName());

        try {
            // Lấy receptionistId từ Invoice (qua appointmentId)
            UUID receptionistId = null;
            try {
                if (event.getAppointmentId() != null) {
                    List<com.main_project.notification_service.dto.response.InvoiceResponseDTO> invoices = 
                        invoiceClient.getInvoicesByAppointmentId(event.getAppointmentId());
                    if (!invoices.isEmpty() && invoices.get(0).getReceptionistId() != null) {
                        try {
                            receptionistId = UUID.fromString(invoices.get(0).getReceptionistId());
                            log.info("Found receptionistId: {} from invoice", receptionistId);
                        } catch (Exception e) {
                            log.warn("Could not parse receptionistId: {}", invoices.get(0).getReceptionistId());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Could not get receptionistId from invoice: {}", e.getMessage());
            }

            // Nếu có receptionistId, lưu notification vào DB
            if (receptionistId != null) {
                try {
                    Notification notification = Notification.builder()
                            .userId(receptionistId)
                            .channel("websocket")
                            .templateId("PRESCRIPTION_DISPENSED")
                            .message(event.getMessage())
                            .status("sent")
                            .retryCount(0)
                            .build();
                    notificationRepository.save(notification);
                    log.info("✅ Saved notification to DB for receptionist: {}", receptionistId);
                } catch (Exception e) {
                    log.error("Failed to save notification for receptionist {}: {}",
                            receptionistId, e.getMessage());
                }
            } else {
                log.warn("No receptionistId found, notification will not be saved to DB");
            }

            // ✅ Gửi WebSocket notification với DTO
            webSocketNotificationService.sendPrescriptionDispensedNotification(
                    event.getDispenseOrderId(),
                    event.getPrescriptionId(),
                    event.getMedicalHistoryId(),
                    event.getAppointmentId(),
                    event.getPharmacistId(),
                    event.getPharmacistName(),
                    event.getMessage()
            );
            
            log.info("✅ Successfully processed prescription dispensed notification");
        } catch (Exception e) {
            log.error("❌ Failed to process prescription dispensed notification: {}", e.getMessage(), e);
        }
    }
}

