package com.main_project.appointment_service.service;

import com.main_project.appointment_service.dto.AppointmentDTO;
import com.main_project.appointment_service.dto.AppointmentRequestDTO;
import com.main_project.appointment_service.enums.AppointmentStatus;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAppointmentService {

    List<AppointmentDTO> getAllAppointments();
    Optional<AppointmentDTO> getAppointmentById(UUID id);
    List<AppointmentDTO> getAppointmentsByDoctorId(UUID doctorId);
    List<AppointmentDTO> getAppointmentsByPatientId(UUID patientId);
    List<AppointmentDTO> getAppointmentsBetween(ZonedDateTime start, ZonedDateTime end);
    List<AppointmentDTO> getAppointmentsByStatus(AppointmentStatus status);
    List<AppointmentDTO> getAppointmentsByMedicalService(UUID medicalServiceId);

    long countAppointmentsByDoctorId(UUID doctorId);
    long countAppointmentsByPatientId(UUID patientId);
    long countAppointmentsByMedicalService(UUID medicalServiceId);

    AppointmentDTO createAppointment(AppointmentRequestDTO requestDTO);
    AppointmentDTO updateAppointment(UUID id, AppointmentRequestDTO requestDTO);
    AppointmentDTO updateAppointmentStatus(UUID id, AppointmentStatus status);
    AppointmentDTO startAppointment(UUID id);

    void deleteAppointment(UUID id);
}
