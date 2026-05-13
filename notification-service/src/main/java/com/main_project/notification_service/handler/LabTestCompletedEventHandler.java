package com.main_project.notification_service.event;

import com.do_an.common.event.LabTestCompletedEvent;
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
public class LabTestCompletedEventHandler {

    private final NotificationRepository notificationRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    @EventHandler
    public void on(LabTestCompletedEvent event) {
        log.info("=== LabTestCompletedEventHandler triggered ===");
        log.info("Received LabTestCompletedEvent for labTestId: {}, appointmentId: {}, doctorId: {}",
                event.getLabTestId(), event.getAppointmentId(), event.getDoctorId());

        if (event.getDoctorId() == null) {
            log.error("DoctorId is null in LabTestCompletedEvent! Cannot process notification.");
            return;
        }

        try {
            UUID doctorId = event.getDoctorId();
            log.info("Processing notification for doctorId: {}", doctorId);

            Notification notification = Notification.builder()
                    .userId(doctorId)
                    .channel("websocket")
                    .templateId("LAB_TEST_COMPLETED")
                    .message(String.format("Xét nghiệm đã hoàn thành. Mã xét nghiệm: %s", event.getLabTestId()))
                    .status("sent")
                    .retryCount(0)
                    .build();

            log.info("Saving notification to database...");
            notification = notificationRepository.save(notification);
            log.info("Notification saved with id: {}, userId: {}, message: {}",
                    notification.getId(), notification.getUserId(), notification.getMessage());

            log.info("Sending websocket notification to doctor: {}", doctorId);
            webSocketNotificationService.sendLabTestCompletedNotification(
                    doctorId,
                    event.getLabTestId(),
                    event.getAppointmentId(),
                    notification.getMessage()
            );

            log.info("✅ Lab test completed notification sent to doctor: {}", doctorId);

        } catch (Exception e) {
            log.error("Failed to process LabTestCompletedEvent for labTestId: {}, appointmentId: {}",
                    event.getLabTestId(), event.getAppointmentId(), e);
            e.printStackTrace();

            try {
                Notification errorNotification = Notification.builder()
                        .userId(event.getDoctorId())
                        .channel("websocket")
                        .templateId("LAB_TEST_COMPLETED")
                        .message(String.format("Xét nghiệm đã hoàn thành. Mã xét nghiệm: %s", event.getLabTestId()))
                        .status("failed")
                        .errorMessage(e.getMessage())
                        .retryCount(0)
                        .build();
                notificationRepository.save(errorNotification);
                log.info("Error notification saved");
            } catch (Exception ex) {
                log.error("Failed to save error notification", ex);
            }
        }
    }
}

