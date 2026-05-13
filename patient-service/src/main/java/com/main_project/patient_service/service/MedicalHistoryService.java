package com.main_project.patient_service.service;

import com.main_project.patient_service.dto.ConditionRequestDTO;
import com.main_project.patient_service.dto.ConditionResponseDTO;
import com.main_project.patient_service.dto.MedicalHistoryRequestDTO;
import com.main_project.patient_service.dto.MedicalHistoryResponseDTO;
import com.main_project.patient_service.entity.Condition;
import com.main_project.patient_service.entity.MedicalHistory;
import com.main_project.patient_service.entity.Patient;
import com.main_project.patient_service.entity.ToothIssue;
import com.main_project.patient_service.repository.MedicalHistoryRepository;
import com.main_project.patient_service.repository.PatientRepository;
import com.main_project.patient_service.util.EntityMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicalHistoryService implements IMedicalHistoryService {

    private final MedicalHistoryRepository medicalHistoryRepository;
    private final PatientRepository patientRepository;
    private final EntityMapper mapper;

    @Override
    public MedicalHistoryResponseDTO createMedicalHistory(MedicalHistoryRequestDTO request) {
        Patient patient = findPatient(request.getPatientId());

        MedicalHistory history = mapper.toMedicalHistoryEntity(request, patient);
        history.setPatient(patient);

        MedicalHistory saved = medicalHistoryRepository.save(history);

        if (request.getConditions() != null) {
            saveToothIssuesFromConditions(patient, request.getConditions());
        }
        
        return mapper.toMedicalHistoryResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalHistoryResponseDTO> getAllMedicalHistories() {
        return medicalHistoryRepository.findAll()
                .stream()
                .map(mapper::toMedicalHistoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalHistoryResponseDTO getMedicalHistoryById(UUID id) {
        return medicalHistoryRepository.findById(id)
                .map(mapper::toMedicalHistoryResponse)
                .orElseThrow(() -> new EntityNotFoundException("Medical history not found for id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalHistoryResponseDTO> getMedicalHistoriesByPatient(UUID patientId) {
        return medicalHistoryRepository.findByPatient_UserId(patientId)
                .stream()
                .map(mapper::toMedicalHistoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalHistoryResponseDTO> getMedicalHistoriesByAppointment(UUID appointmentId) {
        return medicalHistoryRepository.findByAppointmentId(appointmentId)
                .stream()
                .map(mapper::toMedicalHistoryResponse)
                .toList();
    }

    @Override
    public MedicalHistoryResponseDTO updateMedicalHistory(UUID id, MedicalHistoryRequestDTO request) {
        MedicalHistory existing = medicalHistoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medical history not found for id " + id));

        Patient patient = findPatient(request.getPatientId());
        mapper.updateMedicalHistoryEntity(existing, request, patient);

        MedicalHistory saved = medicalHistoryRepository.save(existing);
        if (request.getConditions() != null) {
            saveToothIssuesFromConditions(patient, request.getConditions());
        }
        
        return mapper.toMedicalHistoryResponse(saved);
    }


    private void saveToothIssuesFromConditions(Patient patient, List<ConditionRequestDTO> conditions) {
        for (ConditionRequestDTO condition : conditions) {
            if (condition.getToothNumber() != null) {
                saveToothIssueFromCondition(patient, condition);
            }
        }
    }

    private void saveToothIssueFromCondition(Patient patient, ConditionRequestDTO condition) {
        Integer toothNumber = condition.getToothNumber();
        if (toothNumber == null) {
            return;
        }

        Optional<ToothIssue> existingIssue = patient.getToothIssues().stream()
                .filter(issue -> issue.getToothNumber().equals(toothNumber))
                .findFirst();

        if (existingIssue.isPresent()) {
            ToothIssue issue = existingIssue.get();
            if (condition.getStatus() != null) {
                issue.setStatus(condition.getStatus());
            }
            if (condition.getName() != null && !condition.getName().isEmpty()) {
                issue.setDescription(condition.getName());
            }
            if (condition.getTreatment() != null) {
                issue.setNote(condition.getTreatment());
            } else if (condition.getSurface() != null) {
                issue.setNote(condition.getSurface());
            }
        } else {
            patient.addToothIssue(
                    toothNumber,
                    condition.getStatus() != null ? condition.getStatus() : "ACTIVE",
                    condition.getName() != null ? condition.getName() : "",
                    LocalDate.now(),
                    condition.getTreatment() != null ? condition.getTreatment() : 
                            (condition.getSurface() != null ? condition.getSurface() : "")
            );
        }

        patientRepository.save(patient);
    }


    @Override
    public void deleteMedicalHistory(UUID id) {
        if (!medicalHistoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Medical history not found for id " + id);
        }
        medicalHistoryRepository.deleteById(id);
    }


    @Override
    public ConditionResponseDTO addConditionToMedicalHistory(UUID medicalHistoryId, ConditionRequestDTO conditionRequest) {
        MedicalHistory medicalHistory = medicalHistoryRepository.findById(medicalHistoryId)
                .orElseThrow(() -> new EntityNotFoundException("Medical history not found for id " + medicalHistoryId));

        Condition condition = mapper.toConditionEntity(conditionRequest, medicalHistory);
        medicalHistory.getConditions().add(condition);

        MedicalHistory saved = medicalHistoryRepository.save(medicalHistory);

        Condition savedCondition = saved.getConditions().stream()
                .filter(c -> c.getId().equals(condition.getId()))
                .findFirst()
                .orElse(condition);

        return mapper.toConditionResponse(savedCondition);
    }

    @Override
    public MedicalHistoryResponseDTO updateMedicalHistoryByAppointmentId(MedicalHistoryRequestDTO request) {
        MedicalHistory existing = medicalHistoryRepository.findByAppointmentId(request.getAppointmentId()).stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Medical history not found for appointmentId " + request.getAppointmentId()));

        Patient patient = findPatient(request.getPatientId());
        mapper.updateMedicalHistoryEntity(existing, request, patient);

        MedicalHistory saved = medicalHistoryRepository.save(existing);
        if (request.getConditions() != null) {
            saveToothIssuesFromConditions(patient, request.getConditions());
        }

        return mapper.toMedicalHistoryResponse(saved);
    }

    private Patient findPatient(UUID patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Patient not found for user " + patientId));
    }
}
