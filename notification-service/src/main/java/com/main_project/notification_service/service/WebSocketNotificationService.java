package com.main_project.notification_service.service;

import com.main_project.notification_service.dto.LabTestCompletedNotificationMessage;
import com.main_project.notification_service.dto.LabTestRequestedNotificationMessage;
import com.main_project.notification_service.dto.InvoicePaidNotificationMessage;
import com.main_project.notification_service.dto.PrescriptionDispensedNotificationMessage;
import com.main_project.notification_service.dto.AppointmentCreatedNotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendAppointmentRollbackNotification(UUID appointmentId, String reason) {
        log.info("Sending appointment rollback notification for appointment {} reason={}", appointmentId, reason);

        InvoicePaidNotificationMessage message = new InvoicePaidNotificationMessage(
                "APPOINTMENT_ROLLBACK",
                appointmentId.toString(),
                reason,
                "Bắt đầu khám thất bại. Vui lòng quay lại trang lịch hẹn."
        );

        String topic = "/topic/appointment-rollback/" + appointmentId.toString();
        messagingTemplate.convertAndSend(topic, message);
        log.info("Appointment rollback notification sent to topic {} for appointment {}", topic, appointmentId);
    }

    public void sendInvoicePaidNotification(UUID invoiceId, UUID appointmentId, String message) {
        log.info("Sending invoice paid notification for invoice {} appointment={}", invoiceId, appointmentId);

        // Tạo notification message mở rộng
        InvoicePaidNotificationMessage notification = InvoicePaidNotificationMessage.builder()
                .type("INVOICE_PAID")
                .appointmentId(appointmentId != null ? appointmentId.toString() : null)
                .invoiceId(invoiceId.toString())
                .reason(invoiceId.toString()) // Dùng reason để chứa invoiceId (backward compatible)
                .message(message != null ? message : "Hóa đơn đã được thanh toán thành công. Có thể cấp phát đơn thuốc.")
                .timestamp(System.currentTimeMillis())
                .build();

        // Gửi tới topic chung cho tất cả pharmacists
        String topic = "/topic/invoice-paid";
        messagingTemplate.convertAndSend(topic, notification);

        log.info("Invoice paid notification sent to topic {} for invoice {}", topic, invoiceId);
    }

    public void sendLabTestCompletedNotification(UUID doctorId, UUID labTestId, UUID appointmentId, String message) {
        log.info("=== Sending lab test completed notification ===");
        log.info("Doctor ID: {}, LabTest ID: {}, Appointment ID: {}",
                doctorId, labTestId, appointmentId);
        log.info("Message: {}", message);

        LabTestCompletedNotificationMessage notificationMessage = new LabTestCompletedNotificationMessage(
                "LAB_TEST_COMPLETED",
                labTestId.toString(),
                appointmentId.toString(),
                message
        );

        String topic = "/topic/notifications/" + doctorId.toString();
        log.info("Sending to topic: {}", topic);
        log.info("Notification message object: type={}, labTestId={}, appointmentId={}, message={}",
                notificationMessage.getType(),
                notificationMessage.getLabTestId(),
                notificationMessage.getAppointmentId(),
                notificationMessage.getMessage());

        try {
            messagingTemplate.convertAndSend(topic, notificationMessage);
            log.info("Lab test completed notification sent successfully to topic {} for doctor {}", topic, doctorId);
        } catch (Exception e) {
            log.error("Failed to send websocket notification to topic {}: {}", topic, e.getMessage(), e);
            throw e;
        }
    }

    public void sendPrescriptionDispensedNotification(
            UUID dispenseOrderId,
            UUID prescriptionId,
            UUID medicalHistoryId,
            UUID appointmentId,
            UUID pharmacistId,
            String pharmacistName,
            String message) {
        log.info("Sending prescription dispensed notification for dispenseOrder {} pharmacist={}", 
                dispenseOrderId, pharmacistName);

        // ✅ Tạo DTO thông báo
        PrescriptionDispensedNotificationMessage notification = PrescriptionDispensedNotificationMessage.builder()
                .type("PRESCRIPTION_DISPENSED")
                .dispenseOrderId(dispenseOrderId != null ? dispenseOrderId.toString() : null)
                .prescriptionId(prescriptionId != null ? prescriptionId.toString() : null)
                .medicalHistoryId(medicalHistoryId != null ? medicalHistoryId.toString() : null)
                .appointmentId(appointmentId != null ? appointmentId.toString() : null)
                .pharmacistId(pharmacistId != null ? pharmacistId.toString() : null)
                .pharmacistName(pharmacistName)
                .message(message != null ? message : "Đơn thuốc đã được cấp phát thành công")
                .timestamp(System.currentTimeMillis())
                .build();

        // Gửi tới topic chung cho tất cả receptionists
        String topic = "/topic/prescription-dispensed";
        messagingTemplate.convertAndSend(topic, notification);
        
        log.info("Prescription dispensed notification sent to topic {} for dispenseOrder {}", topic, dispenseOrderId);
    }

    public void sendAppointmentCreatedNotification(
            UUID appointmentId,
            UUID patientId,
            UUID doctorId,
            ZonedDateTime appointmentStartTime,
            ZonedDateTime appointmentEndTime,
            String message) {
        log.info("Sending appointment created notification for appointment {} patient={} doctor={}", 
                appointmentId, patientId, doctorId);

        AppointmentCreatedNotificationMessage notification = AppointmentCreatedNotificationMessage.builder()
                .type("APPOINTMENT_CREATED")
                .appointmentId(appointmentId != null ? appointmentId.toString() : null)
                .patientId(patientId != null ? patientId.toString() : null)
                .doctorId(doctorId != null ? doctorId.toString() : null)
                .appointmentStartTime(appointmentStartTime != null ? appointmentStartTime.toString() : null)
                .appointmentEndTime(appointmentEndTime != null ? appointmentEndTime.toString() : null)
                .message(message != null ? message : "Lịch hẹn mới đã được đăng ký")
                .timestamp(System.currentTimeMillis())
                .build();

        String topic = "/topic/appointment-created";
        messagingTemplate.convertAndSend(topic, notification);
        
        log.info("Appointment created notification sent to topic {} for appointment {}", topic, appointmentId);
    }

    public void sendLabTestRequestedNotification(
            UUID labTestId,
            UUID appointmentId,
            UUID medicalHistoryId,
            UUID doctorId,
            UUID labTestTypeId,
            String message) {
        log.info("Sending lab test requested notification for labTest {} appointment={} doctor={}", 
                labTestId, appointmentId, doctorId);

        LabTestRequestedNotificationMessage notification = LabTestRequestedNotificationMessage.builder()
                .type("LAB_TEST_REQUESTED")
                .labTestId(labTestId != null ? labTestId.toString() : null)
                .appointmentId(appointmentId != null ? appointmentId.toString() : null)
                .medicalHistoryId(medicalHistoryId != null ? medicalHistoryId.toString() : null)
                .doctorId(doctorId != null ? doctorId.toString() : null)
                .labTestTypeId(labTestTypeId != null ? labTestTypeId.toString() : null)
                .message(message != null ? message : "Yêu cầu xét nghiệm mới đã được tạo")
                .timestamp(System.currentTimeMillis())
                .build();

        String topic = "/topic/request-labtest";
        messagingTemplate.convertAndSend(topic, notification);
        
        log.info("Lab test requested notification sent to topic {} for labTest {}", topic, labTestId);
    }

}
