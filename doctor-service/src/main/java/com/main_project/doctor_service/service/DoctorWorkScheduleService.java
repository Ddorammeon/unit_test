package com.main_project.doctor_service.service;

import com.main_project.doctor_service.dto.DoctorWorkScheduleRequestDTO;
import com.main_project.doctor_service.dto.DoctorWorkScheduleResponseDTO;
import com.main_project.doctor_service.entity.Doctor;
import com.main_project.doctor_service.entity.DoctorWorkSchedule;
import com.main_project.doctor_service.entity.WorkSchedule;
import com.main_project.doctor_service.repository.DoctorRepository;
import com.main_project.doctor_service.repository.DoctorWorkScheduleRepository;
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
public class DoctorWorkScheduleService implements IDoctorWorkScheduleService {

    private final DoctorWorkScheduleRepository doctorWorkScheduleRepository;
    private final DoctorRepository doctorRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final EntityMapper mapper;

    @Override
    public DoctorWorkScheduleResponseDTO createDoctorWorkSchedule(DoctorWorkScheduleRequestDTO request) {
        Doctor doctor = getDoctor(request.getDoctorId());
        WorkSchedule workSchedule = getWorkSchedule(request.getWorkScheduleId());
        DoctorWorkSchedule entity = mapper.toDoctorWorkScheduleEntity(request, doctor, workSchedule);
        return mapper.toDoctorWorkScheduleResponse(doctorWorkScheduleRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorWorkScheduleResponseDTO> getAllDoctorWorkSchedules() {
        return doctorWorkScheduleRepository.findAll()
                .stream()
                .map(mapper::toDoctorWorkScheduleResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorWorkScheduleResponseDTO getDoctorWorkScheduleById(UUID id) {
        return doctorWorkScheduleRepository.findById(id)
                .map(mapper::toDoctorWorkScheduleResponse)
                .orElseThrow(() -> new EntityNotFoundException("Doctor work schedule not found for id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorWorkScheduleResponseDTO> getDoctorWorkSchedulesByDoctorId(UUID doctorId) {
        return doctorWorkScheduleRepository.findByDoctor_UserId(doctorId)
                .stream()
                .map(mapper::toDoctorWorkScheduleResponse)
                .toList();
    }

    @Override
    public DoctorWorkScheduleResponseDTO updateDoctorWorkSchedule(UUID id, DoctorWorkScheduleRequestDTO request) {
        DoctorWorkSchedule existing = doctorWorkScheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doctor work schedule not found for id " + id));

        Doctor doctor = getDoctor(request.getDoctorId());
        WorkSchedule workSchedule = getWorkSchedule(request.getWorkScheduleId());
        mapper.updateDoctorWorkScheduleEntity(existing, request, doctor, workSchedule);

        return mapper.toDoctorWorkScheduleResponse(doctorWorkScheduleRepository.save(existing));
    }

    @Override
    public void deleteDoctorWorkSchedule(UUID id) {
        if (!doctorWorkScheduleRepository.existsById(id)) {
            throw new EntityNotFoundException("Doctor work schedule not found for id " + id);
        }
        doctorWorkScheduleRepository.deleteById(id);
    }

    private Doctor getDoctor(UUID doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found for user " + doctorId));
    }

    private WorkSchedule getWorkSchedule(UUID workScheduleId) {
        return workScheduleRepository.findById(workScheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Work schedule not found for id " + workScheduleId));
    }
}
