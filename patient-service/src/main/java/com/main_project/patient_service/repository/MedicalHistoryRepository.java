package com.main_project.patient_service.repository;

import com.main_project.patient_service.entity.MedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, UUID> {
    List<MedicalHistory> findByAppointmentId(UUID appointmentId);
    List<MedicalHistory> findByPatient_UserId(UUID patientId);
}
