package com.main_project.doctor_service.service;

import com.main_project.doctor_service.dto.DoctorDegreeRequestDTO;
import com.main_project.doctor_service.dto.DoctorDegreeResponseDTO;
import com.main_project.doctor_service.entity.Doctor;
import com.main_project.doctor_service.entity.DoctorDegree;
import com.main_project.doctor_service.repository.DoctorDegreeRepository;
import com.main_project.doctor_service.repository.DoctorRepository;
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
public class DoctorDegreeService implements IDoctorDegreeService {

    private final DoctorDegreeRepository doctorDegreeRepository;
    private final DoctorRepository doctorRepository;
    private final EntityMapper mapper;

    @Override
    @Deprecated
    public DoctorDegreeResponseDTO createDoctorDegree(DoctorDegreeRequestDTO request) {
        Doctor doctor = getDoctor(request.getDoctorId());
        DoctorDegree degree = mapper.toDoctorDegreeEntity(request, doctor);
        return mapper.toDoctorDegreeResponse(doctorDegreeRepository.save(degree));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorDegreeResponseDTO> getAllDoctorDegrees() {
        return doctorDegreeRepository.findAll()
                .stream()
                .map(mapper::toDoctorDegreeResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorDegreeResponseDTO getDoctorDegreeById(UUID id) {
        return doctorDegreeRepository.findById(id)
                .map(mapper::toDoctorDegreeResponse)
                .orElseThrow(() -> new EntityNotFoundException("Doctor degree not found for id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorDegreeResponseDTO> getDegreesByDoctorId(UUID doctorId) {
        return doctorDegreeRepository.findByDoctor_UserId(doctorId)
                .stream()
                .map(mapper::toDoctorDegreeResponse)
                .toList();
    }

    @Override
    @Deprecated
    public DoctorDegreeResponseDTO updateDoctorDegree(UUID id, DoctorDegreeRequestDTO request) {
        DoctorDegree existing = doctorDegreeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doctor degree not found for id " + id));
        Doctor doctor = getDoctor(request.getDoctorId());
        mapper.updateDoctorDegreeEntity(existing, request, doctor);
        return mapper.toDoctorDegreeResponse(doctorDegreeRepository.save(existing));
    }

    @Override
    @Deprecated
    public void deleteDoctorDegree(UUID id) {
        if (!doctorDegreeRepository.existsById(id)) {
            throw new EntityNotFoundException("Doctor degree not found for id " + id);
        }
        doctorDegreeRepository.deleteById(id);
    }

    private Doctor getDoctor(UUID doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found for user " + doctorId));
    }
}
