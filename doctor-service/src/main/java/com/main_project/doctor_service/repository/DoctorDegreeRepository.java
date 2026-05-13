package com.main_project.doctor_service.repository;

import com.main_project.doctor_service.entity.DoctorDegree;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * @deprecated This repository violates DDD principles.
 *
 * DoctorDegree is part of the Doctor aggregate and should NOT have its own repository.
 * Doctor is the Aggregate Root - all operations on DoctorDegree must go through Doctor.
 *
 * Use DoctorRepository and Doctor aggregate methods instead:
 * - doctor.addDegree(name, institution, year)
 * - doctor.removeDegree(degree)
 * - doctor.updateDegrees(newDegrees)
 * - doctorRepository.save(doctor)
 *
 * This repository is kept only for read-only queries by the deprecated DoctorDegreeService.
 * DO NOT use this for write operations.
 */
@Deprecated
public interface DoctorDegreeRepository extends JpaRepository<DoctorDegree, UUID> {
    /**
     * Find degrees by doctor ID (read-only query).
     * For modifications, use Doctor aggregate methods.
     */
    List<DoctorDegree> findByDoctor_UserId(UUID doctorId);
}
