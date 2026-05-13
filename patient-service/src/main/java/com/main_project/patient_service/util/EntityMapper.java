package com.main_project.patient_service.util;

import com.main_project.patient_service.dto.ConditionRequestDTO;
import com.main_project.patient_service.dto.ConditionResponseDTO;
import com.main_project.patient_service.dto.MedicalHistoryRequestDTO;
import com.main_project.patient_service.dto.MedicalHistoryResponseDTO;
import com.main_project.patient_service.dto.PatientAllergyDTO;
import com.main_project.patient_service.dto.PatientRequestDTO;
import com.main_project.patient_service.dto.PatientResponseDTO;
import com.main_project.patient_service.dto.ToothIssueDTO;
import com.main_project.patient_service.dto.UnderlyingDiseaseDTO;
import com.main_project.patient_service.entity.Condition;
import com.main_project.patient_service.entity.MedicalHistory;
import com.main_project.patient_service.entity.PatientAllergy;
import com.main_project.patient_service.entity.Patient;
import com.main_project.patient_service.entity.ToothIssue;
import com.main_project.patient_service.entity.UnderlyingDisease;
import com.main_project.patient_service.enums.BloodType;
import com.main_project.patient_service.enums.Gender;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityMapper {

    public PatientResponseDTO toPatientResponse(Patient entity) {
        if (entity == null) return null;

        PatientResponseDTO dto = new PatientResponseDTO();
        dto.setUserId(entity.getUserId());
        dto.setDob(entity.getDob());
        dto.setGender(entity.getGender() != null ? entity.getGender().name() : null);
        dto.setAddress(entity.getAddress());
        dto.setContactPhone(entity.getContactPhone());
        dto.setBloodType(entity.getBloodType() != null ? entity.getBloodType().name() : null);
        dto.setInsuranceNumber(entity.getInsuranceNumber());
        dto.setPatientAllergies(mapPatientAllergiesToDTO(entity.getPatientAllergies()));
        dto.setUnderlyingDiseases(mapUnderlyingDiseasesToDTO(entity.getUnderlyingDiseases()));
        dto.setToothIssues(mapToothIssuesToDTO(entity.getToothIssues()));
        return dto;
    }

    public Patient toPatientEntity(PatientRequestDTO request) {
        if (request == null) return null;

        return Patient.builder()
                .userId(request.getUserId())
                .dob(request.getDob())
                .gender(Gender.fromString(request.getGender()))
                .address(request.getAddress())
                .contactPhone(request.getContactPhone())
                .bloodType(BloodType.fromString(request.getBloodType()))
                .insuranceNumber(request.getInsuranceNumber())
                .build();
    }

    public void updatePatientEntity(Patient entity, PatientRequestDTO request) {
        if (entity == null || request == null) return;

        entity.setDob(request.getDob());
        entity.setGender(Gender.fromString(request.getGender()));
        entity.setAddress(request.getAddress());
        entity.setContactPhone(request.getContactPhone());
        entity.setBloodType(BloodType.fromString(request.getBloodType()));
        entity.setInsuranceNumber(request.getInsuranceNumber());
    }

    public MedicalHistoryResponseDTO toMedicalHistoryResponse(MedicalHistory entity) {
        if (entity == null) return null;

        MedicalHistoryResponseDTO dto = new MedicalHistoryResponseDTO();
        dto.setId(entity.getId());
        dto.setAppointmentId(entity.getAppointmentId());
        dto.setSymptoms(entity.getSymptoms());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getPatient() != null) {
            dto.setPatientId(entity.getPatient().getUserId());
        }

        if (entity.getConditions() != null) {
            dto.setConditions(mapConditionsToDTO(entity.getConditions()));
        }

        return dto;
    }

    public MedicalHistory toMedicalHistoryEntity(MedicalHistoryRequestDTO request, Patient patient) {
        if (request == null) return null;

        MedicalHistory medicalHistory = MedicalHistory.builder()
                .appointmentId(request.getAppointmentId())
                .symptoms(request.getSymptoms())
                .patient(patient)
                .build();

        if (request.getConditions() != null && !request.getConditions().isEmpty()) {
            List<Condition> conditions = request.getConditions().stream()
                    .map(conditionDTO -> toConditionEntity(conditionDTO, medicalHistory))
                    .collect(Collectors.toList());
            medicalHistory.setConditions(new java.util.HashSet<>(conditions));
        }

        return medicalHistory;
    }

    public void updateMedicalHistoryEntity(MedicalHistory entity, MedicalHistoryRequestDTO request, Patient patient) {
        if (entity == null || request == null) return;

        entity.setAppointmentId(request.getAppointmentId());
        entity.setSymptoms(request.getSymptoms());
        entity.setPatient(patient);

        if (request.getConditions() != null) {
            if (!request.getConditions().isEmpty()) {
                List<Condition> conditions = request.getConditions().stream()
                        .map(conditionDTO -> toConditionEntity(conditionDTO, entity))
                        .collect(Collectors.toList());
                entity.getConditions().addAll(conditions);
            }
        }
    }

    public ConditionResponseDTO toConditionResponse(Condition entity) {
        if (entity == null) return null;

        ConditionResponseDTO dto = new ConditionResponseDTO();
        dto.setId(entity.getId());
        dto.setToothNumber(entity.getToothNumber());
        dto.setName(entity.getName());
        dto.setStatus(entity.getStatus());
        dto.setTreatment(entity.getTreatment());
        dto.setSurface(entity.getSurface());

        if (entity.getMedicalHistory() != null) {
            dto.setMedicalHistoryId(entity.getMedicalHistory().getId());
        }

        return dto;
    }

    public Condition toConditionEntity(ConditionRequestDTO request, MedicalHistory medicalHistory) {
        if (request == null) return null;

        return Condition.builder()
                .id(request.getId() != null ? request.getId() : java.util.UUID.randomUUID())
                .medicalHistory(medicalHistory)
                .toothNumber(request.getToothNumber())
                .name(request.getName())
                .status(request.getStatus())
                .treatment(request.getTreatment())
                .surface(request.getSurface())
                .build();
    }

    private List<ConditionResponseDTO> mapConditionsToDTO(Collection<Condition> conditions) {
        if (conditions == null) return null;
        return conditions.stream()
                .map(this::toConditionResponse)
                .collect(Collectors.toList());
    }

    private List<PatientAllergyDTO> mapPatientAllergiesToDTO(Collection<PatientAllergy> allergies) {
        if (allergies == null) return null;
        return allergies.stream()
                .map(pa -> PatientAllergyDTO.builder()
                        .allergyId(pa.getAllergy() != null ? pa.getAllergy().getId() : null)
                        .allergyName(pa.getAllergy() != null ? pa.getAllergy().getName() : null)
                        .severity(pa.getSeverity())
                        .reaction(pa.getReaction())
                        .note(pa.getNote())
                        .build())
                .collect(Collectors.toList());
    }

    private List<UnderlyingDiseaseDTO> mapUnderlyingDiseasesToDTO(Collection<UnderlyingDisease> diseases) {
        if (diseases == null) return null;
        return diseases.stream()
                .map(d -> UnderlyingDiseaseDTO.builder()
                        .name(d.getName())
                        .status(d.getStatus())
                        .severity(d.getSeverity())
                        .isVerified(d.getIsVerified())
                        .note(d.getNote())
                        .build())
                .collect(Collectors.toList());
    }

    private List<ToothIssueDTO> mapToothIssuesToDTO(Collection<ToothIssue> toothIssues) {
        if (toothIssues == null) return null;
        return toothIssues.stream()
                .map(t -> ToothIssueDTO.builder()
                        .toothNumber(t.getToothNumber())
                        .status(t.getStatus())
                        .description(t.getDescription())
                        .diagnosedDate(t.getDiagnosedDate())
                        .note(t.getNote())
                        .build())
                .collect(Collectors.toList());
    }
}