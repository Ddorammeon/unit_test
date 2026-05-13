package com.main_project.doctor_service.service;

import com.main_project.doctor_service.dto.DoctorWorkScheduleRequestDTO;
import com.main_project.doctor_service.dto.DoctorWorkScheduleResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IDoctorWorkScheduleService {
    DoctorWorkScheduleResponseDTO createDoctorWorkSchedule(DoctorWorkScheduleRequestDTO request);

    List<DoctorWorkScheduleResponseDTO> getAllDoctorWorkSchedules();

    DoctorWorkScheduleResponseDTO getDoctorWorkScheduleById(UUID id);

    List<DoctorWorkScheduleResponseDTO> getDoctorWorkSchedulesByDoctorId(UUID doctorId);

    DoctorWorkScheduleResponseDTO updateDoctorWorkSchedule(UUID id, DoctorWorkScheduleRequestDTO request);

    void deleteDoctorWorkSchedule(UUID id);
}
