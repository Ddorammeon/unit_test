package com.main_project.doctor_service.service;

import com.main_project.doctor_service.dto.DoctorRequestDTO;
import com.main_project.doctor_service.dto.DoctorResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IDoctorService {
    DoctorResponseDTO createDoctor(DoctorRequestDTO request);

    List<DoctorResponseDTO> getAllDoctors();

    DoctorResponseDTO getDoctorById(UUID userId);

    DoctorResponseDTO updateDoctor(UUID userId, DoctorRequestDTO request);

    void deleteDoctor(UUID userId);
}
