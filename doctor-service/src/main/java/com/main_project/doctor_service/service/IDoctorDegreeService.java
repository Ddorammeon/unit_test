package com.main_project.doctor_service.service;

import com.main_project.doctor_service.dto.DoctorDegreeRequestDTO;
import com.main_project.doctor_service.dto.DoctorDegreeResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IDoctorDegreeService {
    /**
     * @deprecated Since DoctorDegree has a composition relationship with Doctor,
     * use DoctorService to create/update Doctor with degrees instead.
     */
    @Deprecated
    DoctorDegreeResponseDTO createDoctorDegree(DoctorDegreeRequestDTO request);

    List<DoctorDegreeResponseDTO> getAllDoctorDegrees();

    DoctorDegreeResponseDTO getDoctorDegreeById(UUID id);

    List<DoctorDegreeResponseDTO> getDegreesByDoctorId(UUID doctorId);

    /**
     * @deprecated Since DoctorDegree has a composition relationship with Doctor,
     * use DoctorService to create/update Doctor with degrees instead.
     */
    @Deprecated
    DoctorDegreeResponseDTO updateDoctorDegree(UUID id, DoctorDegreeRequestDTO request);

    /**
     * @deprecated Since DoctorDegree has a composition relationship with Doctor,
     * use DoctorService to update Doctor with modified degrees list instead.
     */
    @Deprecated
    void deleteDoctorDegree(UUID id);
}
