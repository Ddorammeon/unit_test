package com.main_project.doctor_service.repository;

import com.main_project.doctor_service.entity.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, UUID> {
}
