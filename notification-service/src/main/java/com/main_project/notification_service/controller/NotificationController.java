package com.main_project.notification_service.controller;

import com.main_project.notification_service.dto.NotificationDTO;
import com.main_project.notification_service.entity.Notification;
import com.do_an.common.event.InvoicePaidNotificationEvent;
import com.main_project.notification_service.dto.request.InvoicePaidRequest;
import com.main_project.notification_service.repository.NotificationRepository;
import com.main_project.notification_service.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventBus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.axonframework.eventhandling.GenericEventMessage.asEventMessage;



@RestController
@RequestMapping("/notification-service/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    private final EventBus eventBus;
    private final WebSocketNotificationService webSocketNotificationService;
    private final NotificationRepository notificationRepository;

    @PostMapping("/test/invoice-paid")
    public ResponseEntity<Map<String, Object>> simulateInvoicePaid(
            @RequestBody(required = false) InvoicePaidRequest request) {

        log.info("🔔 [TEST API] Simulating InvoicePaidNotificationEvent");


        // Tạo event
        InvoicePaidNotificationEvent event = new InvoicePaidNotificationEvent(
                UUID.fromString(request.getInvoiceId()),
                UUID.fromString(request.getAppointmentId()),
                "Hóa đơn " + request.getInvoiceId() + " đã được thanh toán thành công. Có thể cấp phát đơn thuốc."
        );

        // Phát event qua Axon EventBus
        eventBus.publish(asEventMessage(event));

        log.info("✅ [TEST API] InvoicePaidNotificationEvent published: invoiceId={}, appointmentId={}",
                request.getInvoiceId(), request.getAppointmentId());

        // Trả về response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "InvoicePaidNotificationEvent đã được phát thành công");

        return ResponseEntity.ok(response);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByUserId(@PathVariable UUID userId) {
        log.info("Getting notifications for userId: {}", userId);
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        log.info("Found {} notifications for userId: {}", notifications.size(), userId);
        List<NotificationDTO> dtos = notifications.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable UUID userId) {
        log.info("Getting unread count for userId: {}", userId);
        long count = notificationRepository.countByUserIdAndStatus(userId, "sent");
        log.info("Unread count for userId {}: {}", userId, count);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/template")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByTemplateId(@RequestParam String templateId) {
        log.info("Getting notifications for templateId: {}", templateId);
        List<Notification> notifications = notificationRepository.findByTemplateIdOrderByCreatedAtDesc(templateId);
        log.info("Found {} notifications for templateId: {}", notifications.size(), templateId);
        List<NotificationDTO> dtos = notifications.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        notificationRepository.findById(id).ifPresent(notification -> {
            notification.setStatus("read");
            notificationRepository.save(notification);
        });
        return ResponseEntity.ok().build();
    }

    private NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .channel(notification.getChannel())
                .templateId(notification.getTemplateId())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .errorMessage(notification.getErrorMessage())
                .retryCount(notification.getRetryCount())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
                
    }
}
