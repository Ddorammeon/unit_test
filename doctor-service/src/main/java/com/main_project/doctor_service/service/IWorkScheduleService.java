package com.main_project.doctor_service.service;

import com.main_project.doctor_service.dto.WorkScheduleRequestDTO;
import com.main_project.doctor_service.dto.WorkScheduleResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IWorkScheduleService {
    WorkScheduleResponseDTO createWorkSchedule(WorkScheduleRequestDTO request);

    List<WorkScheduleResponseDTO> getAllWorkSchedules();

    WorkScheduleResponseDTO getWorkScheduleById(UUID id);

    WorkScheduleResponseDTO updateWorkSchedule(UUID id, WorkScheduleRequestDTO request);

    void deleteWorkSchedule(UUID id);
}
