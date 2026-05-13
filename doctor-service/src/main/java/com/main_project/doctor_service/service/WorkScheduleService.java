package com.main_project.doctor_service.service;

import com.main_project.doctor_service.dto.WorkScheduleRequestDTO;
import com.main_project.doctor_service.dto.WorkScheduleResponseDTO;
import com.main_project.doctor_service.entity.WorkSchedule;
import com.main_project.doctor_service.repository.WorkScheduleRepository;
import com.main_project.doctor_service.util.EntityMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkScheduleService implements IWorkScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final EntityMapper mapper;

    @Override
    public WorkScheduleResponseDTO createWorkSchedule(WorkScheduleRequestDTO request) {
        WorkSchedule schedule = mapper.toWorkScheduleEntity(request);
        return mapper.toWorkScheduleResponse(workScheduleRepository.save(schedule));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkScheduleResponseDTO> getAllWorkSchedules() {
        return workScheduleRepository.findAll()
                .stream()
                .map(mapper::toWorkScheduleResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkScheduleResponseDTO getWorkScheduleById(UUID id) {
        return workScheduleRepository.findById(id)
                .map(mapper::toWorkScheduleResponse)
                .orElseThrow(() -> new EntityNotFoundException("Work schedule not found for id " + id));
    }

    @Override
    public WorkScheduleResponseDTO updateWorkSchedule(UUID id, WorkScheduleRequestDTO request) {
        WorkSchedule existing = workScheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Work schedule not found for id " + id));
        mapper.updateWorkScheduleEntity(existing, request);
        return mapper.toWorkScheduleResponse(workScheduleRepository.save(existing));
    }

    @Override
    public void deleteWorkSchedule(UUID id) {
        if (!workScheduleRepository.existsById(id)) {
            throw new EntityNotFoundException("Work schedule not found for id " + id);
        }
        workScheduleRepository.deleteById(id);
    }
}
