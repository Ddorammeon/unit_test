package com.main_project.doctor_service.service;

import com.main_project.doctor_service.dto.DoctorRequestDTO;
import com.main_project.doctor_service.dto.DoctorResponseDTO;
import com.main_project.doctor_service.entity.Doctor;
import com.main_project.doctor_service.repository.DoctorRepository;
import com.main_project.doctor_service.util.EntityMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * DoctorService - Application Service for the Doctor Aggregate
 *
 * This service follows DDD principles:
 * - Doctor is the Aggregate Root
 * - All operations on DoctorDegree go through the Doctor entity methods
 * - Only DoctorRepository is used (no DoctorDegreeRepository for write operations)
 * - Cascade and orphanRemoval ensure consistency
 *
 * Usage:
 * - Create doctor with degrees: Uses doctor.addDegree() internally
 * - Update doctor: Uses doctor.updateBasicInfo() and doctor.updateDegrees()
 * - Delete doctor: Cascades to all degrees automatically
 * - All changes persisted via doctorRepository.save(doctor)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DoctorService implements IDoctorService {

    private final DoctorRepository doctorRepository;
    private final EntityMapper mapper;

    /**
     * Creates a new doctor with degrees.
     * Degrees are added using doctor.addDegree() aggregate method.
     * All data is persisted via doctorRepository.save(doctor).
     */
    @Override
    public DoctorResponseDTO createDoctor(DoctorRequestDTO request) {
        if (doctorRepository.existsById(request.getUserId())) {
            throw new DataIntegrityViolationException("Doctor already exists for user " + request.getUserId());
        }
        Doctor doctor = mapper.toDoctorEntity(request);
        return mapper.toDoctorResponse(doctorRepository.save(doctor));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponseDTO> getAllDoctors() {
        return doctorRepository.findAll()
                .stream()
                .map(mapper::toDoctorResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponseDTO getDoctorById(UUID userId) {
        return doctorRepository.findById(userId)
                .map(mapper::toDoctorResponse)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found for user " + userId));
    }

    /**
     * Updates a doctor and its degrees.
     * Uses doctor.updateBasicInfo() and doctor.updateDegrees() aggregate methods.
     * Orphaned degrees are automatically removed due to orphanRemoval=true.
     */
    @Override
    public DoctorResponseDTO updateDoctor(UUID userId, DoctorRequestDTO request) {
        Doctor doctor = doctorRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found for user " + userId));

        mapper.updateDoctorEntity(doctor, request);
        return mapper.toDoctorResponse(doctorRepository.save(doctor));
    }

    /**
     * Deletes a doctor and all associated degrees.
     * Cascade delete handles removal of all degrees automatically.
     */
    @Override
    public void deleteDoctor(UUID userId) {
        if (!doctorRepository.existsById(userId)) {
            throw new EntityNotFoundException("Doctor not found for user " + userId);
        }
        doctorRepository.deleteById(userId);
    }
}
