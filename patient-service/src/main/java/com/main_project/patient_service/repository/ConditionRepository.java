package com.main_project.patient_service.repository;

import com.main_project.patient_service.entity.Condition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConditionRepository extends JpaRepository<Condition, UUID> {
    List<Condition> findByMedicalHistory_Id(UUID medicalHistoryId);
}