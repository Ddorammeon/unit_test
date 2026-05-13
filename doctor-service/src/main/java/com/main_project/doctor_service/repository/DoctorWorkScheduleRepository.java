package com.main_project.doctor_service.repository;

import com.main_project.doctor_service.entity.DoctorWorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DoctorWorkScheduleRepository extends JpaRepository<DoctorWorkSchedule, UUID> {
    List<DoctorWorkSchedule> findByDoctor_UserId(UUID doctorId);
    List<DoctorWorkSchedule> findByWorkSchedule_Id(UUID workScheduleId);
}
