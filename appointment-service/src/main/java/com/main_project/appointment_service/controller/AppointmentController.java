package com.main_project.appointment_service.controller;

import com.do_an.common.command.StartAppointmentCommand;
import com.do_an.common.model.MedicalServiceDTO;
import com.main_project.appointment_service.aggregate.CheckInAppointmentCommand;
import com.main_project.appointment_service.dto.AppointmentDTO;
import com.main_project.appointment_service.dto.AppointmentRequestDTO;
import com.main_project.appointment_service.dto.HoldSlotRequestDTO;
import com.main_project.appointment_service.enums.AppointmentStatus;
import com.main_project.appointment_service.service.AppointmentService;
import com.main_project.appointment_service.service.SlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/appointment-service/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final SlotService slotService;
    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;


    //Dùng
    @PostMapping("/{id}/check-in")
    @Operation(summary = "Patient checks in for the appointment, creating the initial aggregate")
    public ResponseEntity<Void> checkInAppointment(@PathVariable UUID id) {
        log.info("[AppointmentController] Patient requests check-in for appointment {}", id);

        AppointmentDTO appointment = appointmentService.updateAppointmentStatus(id, AppointmentStatus.CHECKED);
        CheckInAppointmentCommand command = new CheckInAppointmentCommand(appointment.getId());

        commandGateway.send(command)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("[AppointmentController] CheckInAppointmentCommand failed for appointment {}: {}", appointment.getId(), error.getMessage(), error);
                    } else {
                        log.info("[AppointmentController] CheckInAppointmentCommand accepted for appointment {}", appointment.getId());
                    }
                });

        return ResponseEntity.accepted().build();
    }


    //Dùng
    @PostMapping("/{id}/start")
    @Operation(summary = "Doctor starts the consultation for a checked-in appointment")
    public ResponseEntity<Void> startConsultation(@PathVariable UUID id) {
        log.info("[AppointmentController] Doctor requests to start consultation for appointment {}", id);
        AppointmentDTO appointment = appointmentService.startAppointment(id);

        UUID clinicalId = UUID.randomUUID();
        List<MedicalServiceDTO> medicalServicePayload = appointment.getMedicalServices() == null
                ? List.of()
                : appointment.getMedicalServices().stream()
                .map(this::toCommonMedicalServiceDTO)
                .toList();
        List<MedicalServiceDTO> serializedServices = new ArrayList<>(medicalServicePayload);

        log.info("[AppointmentController] Dispatch StartAppointmentCommand appointmentId={}, clinicalId={}, patientId={}, doctorId={}, services={}",
                appointment.getId(), clinicalId, appointment.getPatientId(), appointment.getDoctorId(), medicalServicePayload.size());
        StartAppointmentCommand command = new StartAppointmentCommand(
                appointment.getId(),
                clinicalId,
                appointment.getPatientId(),
                appointment.getDoctorId(),
                serializedServices
        );

        commandGateway.send(command);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);

    }


    //Dùng
    @PostMapping("/slots/hold")
    @Operation(summary = "Giữ slot trong 10 phút trước khi tạo lịch hẹn")
    public ResponseEntity<?> holdSlot(@RequestBody HoldSlotRequestDTO request) {

        if (request.getMedicalServiceIds() == null || request.getMedicalServiceIds().isEmpty()) {
            return ResponseEntity.badRequest().body("Thiếu danh sách dịch vụ để tính thời lượng");
        }

        int durationMinutes;
        try {
            durationMinutes = slotService.calculateServiceDurationMinutes(request.getMedicalServiceIds());
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }

        ZonedDateTime slotStart = request.getAppointmentStartTime();
        ZonedDateTime slotEnd = slotStart.plusMinutes(durationMinutes);

        if (!slotService.isSlotAvailable(request.getDoctorId(), slotStart, slotEnd)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Slot đã được giữ hoặc trùng lịch, vui lòng chọn thời gian khác");
        }

        boolean locked = slotService.lockSlot(
                request.getDoctorId(),
                request.getPatientId(),
                slotStart,
                slotEnd
        );

        if (!locked) {
            return ResponseEntity.badRequest().body("Slot đã được giữ bởi người khác, vui lòng chọn slot khác");
        }

        // Có thể trả thêm expiredAt = now + 10 phút cho FE đếm ngược
        return ResponseEntity.ok().build();
    }

    //Dùng
    @PostMapping("/slots/release")
    @Operation(summary = "Nhả slot đã giữ khi người dùng thay đổi lựa chọn hoặc đóng dialog")
    public ResponseEntity<?> releaseSlot(@RequestBody HoldSlotRequestDTO request) {
        if (request.getDoctorId() == null || request.getAppointmentStartTime() == null) {
            return ResponseEntity.badRequest().body("Thiếu thông tin doctorId hoặc appointmentStartTime");
        }

        slotService.unlockSlot(request.getDoctorId(), request.getAppointmentStartTime());
        return ResponseEntity.ok().build();
    }



    //Dùng
    @GetMapping
    @Operation(summary = "Lấy tất cả lịch hẹn")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    //Dùng
    @GetMapping("/{id}")
    @Operation(summary = "Lấy lịch hẹn theo ID")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable UUID id) {
        Optional<AppointmentDTO> dto = appointmentService.getAppointmentById(id);
        return dto.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }


    //Dùng
    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Lấy lịch hẹn theo bác sĩ")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByDoctorId(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctorId(doctorId));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Lấy lịch hẹn theo bệnh nhân")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByPatientId(@PathVariable UUID patientId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatientId(patientId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Lấy lịch hẹn theo trạng thái")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByStatus(@PathVariable AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByStatus(status));
    }

    @GetMapping("/range")
    @Operation(summary = "Lấy lịch hẹn trong khoảng thời gian")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsBetween(
            @RequestParam ZonedDateTime start,
            @RequestParam ZonedDateTime end) {
        return ResponseEntity.ok(appointmentService.getAppointmentsBetween(start, end));
    }


    //Dùng
    @GetMapping("/date")
    @Operation(summary = "Lấy lịch hẹn theo ngày (dd/mm/yyyy)", description = "Trả về danh sách ca làm việc trong ngày được chỉ định")
    @Parameter(name = "date", description = "Ngày cần lấy ca làm việc", required = true)
    public ResponseEntity<List<AppointmentDTO>> getByDate(@RequestParam("date") String date) {
        // 1. Parse String "dd/MM/yyyy" → LocalDate
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate localDate;
        try {
            localDate = LocalDate.parse(date, formatter);
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest()
                    .body(Collections.emptyList()); // hoặc trả lỗi chi tiết
        }

        // 2. Chuyển LocalDate → ZonedDateTime start-of-day và end-of-day
        ZonedDateTime startOfDay = localDate.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime endOfDay   = localDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault());

        // 3. Gọi service (service dùng repository query ZonedDateTime)
        List<AppointmentDTO> appointments = appointmentService.getAppointmentsBetween(startOfDay, endOfDay);

        return ResponseEntity.ok(appointments);
    }


    //Dùng
    @PostMapping
    @Operation(summary = "Tạo lịch hẹn mới")
    public ResponseEntity<AppointmentDTO> createAppointment(@Valid @RequestBody AppointmentRequestDTO requestDTO) {
        AppointmentDTO created = appointmentService.createAppointment(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật lịch hẹn")
    public ResponseEntity<AppointmentDTO> updateAppointment(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentRequestDTO requestDTO) {
        AppointmentDTO updated = appointmentService.updateAppointment(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    //Dùng
    @PatchMapping("/{id}/status")
    @Operation(summary = "Cập nhật trạng thái lịch hẹn")
    public ResponseEntity<AppointmentDTO> updateStatus(
            @PathVariable UUID id,
            @RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.updateAppointmentStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa lịch hẹn theo ID")
    public ResponseEntity<Void> deleteAppointment(@PathVariable UUID id) {
        try {
            appointmentService.deleteAppointment(id);
            return ResponseEntity.noContent().build(); // 204 OK
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500
        }
    }



    private MedicalServiceDTO toCommonMedicalServiceDTO(com.main_project.appointment_service.dto.MedicalServiceDTO serviceDTO) {
        if (serviceDTO == null) {
            return null;
        }
        MedicalServiceDTO dto = new MedicalServiceDTO();
        dto.setId(serviceDTO.getId());
        dto.setServiceName(serviceDTO.getServiceName());
        dto.setServiceType(serviceDTO.getServiceType());
        dto.setServiceTime(serviceDTO.getServiceTime());
        dto.setStatus(serviceDTO.getStatus() != null
                ? com.do_an.common.model.MedicalServiceStatus.valueOf(serviceDTO.getStatus().name())
                : null);
        dto.setPrice(serviceDTO.getPrice());
        dto.setDescription(serviceDTO.getDescription());
        dto.setImgUrl(serviceDTO.getImgUrl());
        return dto;
    }
}





















