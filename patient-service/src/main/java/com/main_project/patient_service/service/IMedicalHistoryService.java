package com.main_project.patient_service.service;

import com.main_project.patient_service.dto.ConditionResponseDTO;
import com.main_project.patient_service.dto.MedicalHistoryRequestDTO;
import com.main_project.patient_service.dto.MedicalHistoryResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IMedicalHistoryService {
    MedicalHistoryResponseDTO createMedicalHistory(MedicalHistoryRequestDTO request);

    List<MedicalHistoryResponseDTO> getAllMedicalHistories();

    MedicalHistoryResponseDTO getMedicalHistoryById(UUID id);

    List<MedicalHistoryResponseDTO> getMedicalHistoriesByPatient(UUID patientId);

    List<MedicalHistoryResponseDTO> getMedicalHistoriesByAppointment(UUID appointmentId);

    MedicalHistoryResponseDTO updateMedicalHistory(UUID id, MedicalHistoryRequestDTO request);

    void deleteMedicalHistory(UUID id);

    ConditionResponseDTO addConditionToMedicalHistory(UUID medicalHistoryId, com.main_project.patient_service.dto.ConditionRequestDTO conditionRequest);

    MedicalHistoryResponseDTO updateMedicalHistoryByAppointmentId(MedicalHistoryRequestDTO request);
}
