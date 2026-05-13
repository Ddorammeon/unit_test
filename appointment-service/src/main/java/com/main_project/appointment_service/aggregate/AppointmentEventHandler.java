package com.main_project.appointment_service.aggregate;

import com.do_an.common.event.AppointmentRolledBackEvent;
import com.do_an.common.event.InvoicePaidEvent;
import com.main_project.appointment_service.dto.InvoiceResponseDTO;
import com.main_project.appointment_service.entity.Appointment;
import com.main_project.appointment_service.enums.AppointmentStatus;
import com.main_project.appointment_service.feignclient.InvoiceServiceClient;
import com.main_project.appointment_service.repository.AppointmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentEventHandler {

    private final AppointmentRepository appointmentRepository;
    private final InvoiceServiceClient invoiceServiceClient;

    @EventHandler
    @Transactional
    public void on(AppointmentRolledBackEvent event) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(event.getAppointmentId());

        if (appointmentOpt.isEmpty()) {
            log.warn("Không tìm thấy appointment {} để rollback", event.getAppointmentId());
            return;
        }

        Appointment appointment = appointmentOpt.get();
        appointment.setStatus(AppointmentStatus.CHECKED);
        appointment.setUpdatedAt(ZonedDateTime.now());
        appointmentRepository.save(appointment);

        log.info("Appointment {} đã được cập nhật trạng thái FAILED sau rollback", event.getAppointmentId());
    }

    @EventHandler
    public void on(InvoicePaidEvent event){
        InvoiceResponseDTO invoiceResponseDTO = invoiceServiceClient.getInvoiceById(event.getInvoiceId());
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(invoiceResponseDTO.getAppointmentId());
        if (appointmentOpt.isEmpty()) {
            log.warn("Không tìm thấy appointment {}", invoiceResponseDTO.getAppointmentId());
            return;
        }
        Appointment appointment = appointmentOpt.get();
        appointment.setStatus(AppointmentStatus.COMPLETED_INVOICE);
        appointment.setUpdatedAt(ZonedDateTime.now());
        appointmentRepository.save(appointment);
        log.info("Appointment {} đã được cập nhật trạng thái COMPLETED_INVOICE cho hoá đơn {}", invoiceResponseDTO.getAppointmentId(), event.getInvoiceId());

    }
}

