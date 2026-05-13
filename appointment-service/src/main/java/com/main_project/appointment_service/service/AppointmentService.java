package com.main_project.appointment_service.service;

import com.main_project.appointment_service.dto.AppointmentDTO;
import com.main_project.appointment_service.dto.AppointmentRequestDTO;
import com.main_project.appointment_service.dto.UserDTO;
import com.main_project.appointment_service.entity.Appointment;
import com.main_project.appointment_service.entity.MedicalService;
import com.main_project.appointment_service.enums.AppointmentStatus;
import com.main_project.appointment_service.exceptions.AppException;
import com.main_project.appointment_service.exceptions.enums.ErrorCode;
import com.main_project.appointment_service.feignclient.UserServiceClient;
import com.main_project.appointment_service.repository.AppointmentRepository;
import com.main_project.appointment_service.repository.MedicalServiceRepository;
import com.main_project.appointment_service.util.EntityDTOMapper;
import com.do_an.common.event.AppointmentCreatedEvent;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.axonframework.eventhandling.EventBus;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.main_project.appointment_service.exceptions.enums.ErrorCode.APPINTMENT_IS_NOT_CHECKIN_YET;
import static com.main_project.appointment_service.exceptions.enums.ErrorCode.APPOINTMENT_NOT_EXISTED;
import static org.axonframework.eventhandling.GenericEventMessage.asEventMessage;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AppointmentService implements IAppointmentService {
    private final UserServiceClient userServiceClient;
    private final AppointmentRepository appointmentRepository;
    private final MedicalServiceRepository medicalServiceRepository;

    private final SlotService slotService;

    private final EntityDTOMapper mapper;
    private final EventBus eventBus;

    @Override
    public List<AppointmentDTO> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(mapper::toAppointmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AppointmentDTO> getAppointmentById(UUID id) {
        return appointmentRepository.findById(id)
                .map(mapper::toAppointmentDTO);
    }

    @Override
    public List<AppointmentDTO> getAppointmentsByDoctorId(UUID doctorId) {
        return appointmentRepository.findByDoctorId(doctorId)
                .stream()
                .map(mapper::toAppointmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentDTO> getAppointmentsByPatientId(UUID patientId) {
        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .map(mapper::toAppointmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentDTO> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status)
                .stream()
                .map(mapper::toAppointmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentDTO> getAppointmentsBetween(ZonedDateTime start, ZonedDateTime end) {
        return appointmentRepository.findByAppointmentStartTimeBetween(start, end)
                .stream()
                .map(mapper::toAppointmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentDTO> getAppointmentsByMedicalService(UUID medicalServiceId) {
        return appointmentRepository.findByMedicalServiceId(medicalServiceId)
                .stream()
                .map(mapper::toAppointmentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long countAppointmentsByDoctorId(UUID doctorId) {
        return appointmentRepository.countByDoctorId(doctorId);
    }

    @Override
    public long countAppointmentsByPatientId(UUID patientId) {
        return appointmentRepository.countByPatientId(patientId);
    }

    @Override
    public long countAppointmentsByMedicalService(UUID medicalServiceId) {
        return appointmentRepository.countByMedicalServiceId(medicalServiceId);
    }

    // ==============================
    // 🔹 CREATE
    // ==============================
    @Override
    public AppointmentDTO createAppointment(AppointmentRequestDTO requestDTO) {
        if (requestDTO.getMedicalServiceIds() == null || requestDTO.getMedicalServiceIds().isEmpty()) {
            throw new AppException(ErrorCode.LIST_MEDICAL_SERVICE_EMPTY);
        }

        // ============================================
        // 🔹 1. LOAD MEDICAL SERVICES
        // ============================================
        List<MedicalService> medicalServices =
                medicalServiceRepository.findAllById(requestDTO.getMedicalServiceIds());

        if (medicalServices.size() != requestDTO.getMedicalServiceIds().size()) {
            throw new AppException(ErrorCode.INVALID_MEDICAL_SERVICE_ID);
        }

        int totalServiceTime = medicalServices.stream()
                .mapToInt(MedicalService::getServiceTime)
                .sum();

        ZonedDateTime start = requestDTO.getAppointmentStartTime();
        ZonedDateTime end = start.plusMinutes(totalServiceTime);

        requestDTO.setAppointmentEndTime(end);

        // ============================================
        // 🔹 2. CHECK & LOCK SLOT (Redis)
        // ============================================
        // 2. Kiểm tra slot có đang được giữ bởi đúng patient không
        boolean available = slotService.validateAndUnlockSlot(
                requestDTO.getDoctorId(),
                requestDTO.getPatientId(),
                requestDTO.getAppointmentStartTime(),
                end
        );

        if (!available) {
            throw new RuntimeException("Slot đã hết hạn hoặc không hợp lệ, vui lòng đặt lại từ đầu");
        }

        try {
            // ============================================
            // 🔹 3. CHECK DB TRÙNG (chống race-condition)
            // ============================================
            List<Appointment> overlapping = appointmentRepository
                    .findOverlappingAppointments(
                            requestDTO.getDoctorId(),
                            start,
                            end
                    );

            if (!overlapping.isEmpty()) {
                // nếu DB có trùng -> phải unlock trước khi throw
                slotService.unlockSlot(requestDTO.getDoctorId(), start);
                throw new RuntimeException("The selected time slot is not available");
            }

            // ============================================
            // 🔹 4. TẠO APPOINTMENT
            // ============================================
            Appointment appointment = mapper.toAppointmentEntity(requestDTO, medicalServices);
            appointment.setCreatedAt(ZonedDateTime.now());
            appointment.setUpdatedAt(ZonedDateTime.now());
            appointment.setStatus(AppointmentStatus.CONFIRMED);

            appointmentRepository.save(appointment);

            // Slot đã book thành công → có thể giữ nguyên lock cho đến khi expire,
            // hoặc xóa luôn key:
            slotService.unlockSlot(requestDTO.getDoctorId(), start);

            AppointmentDTO appointmentDTO = mapper.toAppointmentDTO(appointment);
            try {
                String message = String.format(
                    "Lịch hẹn mới đã được đăng ký. Mã lịch hẹn: %s",
                    appointment.getId().toString().substring(0, 8)
                );
                
                AppointmentCreatedEvent event = new AppointmentCreatedEvent(
                    appointment.getId(),
                    appointment.getPatientId(),
                    appointment.getDoctorId(),
                    appointment.getAppointmentStartTime(),
                    appointment.getAppointmentEndTime(),
                    message
                );
                
                eventBus.publish(asEventMessage(event));
                log.info(" Published AppointmentCreatedEvent for appointment {}", appointment.getId());
            } catch (Exception e) {
                log.error(" Failed to publish AppointmentCreatedEvent: {}", e.getMessage(), e);
            }

            return appointmentDTO;

        } catch (RuntimeException ex) {

            // Bắt tất cả lỗi và đảm bảo unlock để tránh dead-lock
            slotService.unlockSlot(requestDTO.getDoctorId(), start);
            throw ex;
        }
    }

    // ==============================
    // 🔹 UPDATE
    // ==============================
    @Override
    public AppointmentDTO updateAppointment(UUID id, AppointmentRequestDTO requestDTO) {
        Appointment existing = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        List<MedicalService> medicalServices = List.of();
        if (requestDTO.getMedicalServiceIds() != null && !requestDTO.getMedicalServiceIds().isEmpty()) {
            medicalServices = medicalServiceRepository.findAllById(requestDTO.getMedicalServiceIds());
            if (medicalServices.size() != requestDTO.getMedicalServiceIds().size()) {
                throw new RuntimeException("Some medical services not found");
            }
        }
        mapper.updateAppointmentEntity(existing, requestDTO, medicalServices);
        existing.setUpdatedAt(ZonedDateTime.now());

        appointmentRepository.save(existing);
        return mapper.toAppointmentDTO(existing);
    }

    // ==============================
    // 🔹 UPDATE STATUS
    // ==============================
    @Override
    public AppointmentDTO updateAppointmentStatus(UUID id, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.setStatus(status);
        appointment.setUpdatedAt(ZonedDateTime.now());
        appointmentRepository.save(appointment);
        return mapper.toAppointmentDTO(appointment);
    }

    @Override
    public AppointmentDTO startAppointment(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException(APPOINTMENT_NOT_EXISTED));

        log.info("[AppointmentService] Start appointment request id={}, currentStatus={}", id, appointment.getStatus());
        if (appointment.getStatus() != AppointmentStatus.CHECKED) {
            throw new AppException(APPINTMENT_IS_NOT_CHECKIN_YET);
        }

        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        appointment.setUpdatedAt(ZonedDateTime.now());
        appointmentRepository.save(appointment);
        log.info("[AppointmentService] Appointment {} moved to IN_PROGRESS at {}", id, appointment.getUpdatedAt());
        return mapper.toAppointmentDTO(appointment);
    }

    // ==============================
    // 🔹 DELETE
    // ==============================
    @Override
    public void deleteAppointment(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(ZonedDateTime.now());
        appointmentRepository.save(appointment);
    }

    public void validateDoctor(UUID doctorId) {
        UserDTO doctor;
        try {
            doctor = userServiceClient.getUserById(doctorId);
        } catch (Exception e) {
            throw new EntityNotFoundException("Doctor with ID " + doctorId + " not found.");
        }
        if (doctor == null) {
            throw new RuntimeException("Doctor not found");
        }
        if (!doctor.getRoles().contains("DOCTOR")) {
            throw new RuntimeException("User không phải bác sĩ");
        }

    }

    public void validatePatient(UUID patientId) {
        UserDTO patient;
        try {
            patient = userServiceClient.getUserById(patientId);
        } catch (Exception e) {
            throw new EntityNotFoundException("Patient with ID " + patientId + " not found.");
        }
        if (patient == null) {
            throw new RuntimeException("Patient not found");
        }

        if (!patient.getRoles().contains("PATIENT"))
            throw new RuntimeException("User không phải bệnh nhân");
    }
}
