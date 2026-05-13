package com.main_project.doctor_service.util;

import com.main_project.doctor_service.dto.*;
import com.main_project.doctor_service.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EntityMapper {

    // Doctor
    public DoctorResponseDTO toDoctorResponse(Doctor entity) {
        if (entity == null) return null;
        DoctorResponseDTO dto = new DoctorResponseDTO();
        dto.setUserId(entity.getUserId());
        dto.setSpecializationCode(entity.getSpecializationCode());
        dto.setWorkingHospital(entity.getWorkingHospital());
        dto.setLicenseNumber(entity.getLicenseNumber());
        dto.setConsultationFeeAmount(entity.getConsultationFeeAmount());

        if (entity.getDegrees() != null) {
            entity.getDegrees().forEach(degree ->
                dto.getDegrees().add(toDoctorDegreeResponse(degree))
            );
        }

        return dto;
    }

    public Doctor toDoctorEntity(DoctorRequestDTO request) {
        if (request == null) return null;
        Doctor doctor = Doctor.builder()
                .userId(request.getUserId())
                .specializationCode(request.getSpecializationCode())
                .workingHospital(request.getWorkingHospital())
                .licenseNumber(request.getLicenseNumber())
                .consultationFeeAmount(request.getConsultationFeeAmount())
                .build();

        // Use aggregate root method to add degrees
        if (request.getDegrees() != null && !request.getDegrees().isEmpty()) {
            request.getDegrees().forEach(degreeDTO ->
                doctor.addDegree(
                    degreeDTO.getDegreeName(),
                    degreeDTO.getInstitution(),
                    degreeDTO.getYearObtained()
                )
            );
        }

        return doctor;
    }

    public void updateDoctorEntity(Doctor entity, DoctorRequestDTO request) {
        if (entity == null || request == null) return;

        // Update basic info using aggregate method
        entity.updateBasicInfo(
            request.getSpecializationCode(),
            request.getWorkingHospital(),
            request.getLicenseNumber(),
            request.getConsultationFeeAmount()
        );

        // Update degrees using aggregate method
        if (request.getDegrees() != null) {
            List<Doctor.DegreeData> degreeDataList = request.getDegrees().stream()
                .map(dto -> new Doctor.DegreeData(
                    dto.getDegreeName(),
                    dto.getInstitution(),
                    dto.getYearObtained()
                ))
                .toList();
            entity.updateDegrees(degreeDataList);
        } else {
            entity.clearDegrees();
        }
    }

    // DoctorDegree
    public DoctorDegreeResponseDTO toDoctorDegreeResponse(DoctorDegree entity) {
        if (entity == null) return null;
        DoctorDegreeResponseDTO dto = new DoctorDegreeResponseDTO();
        dto.setId(entity.getId());
        dto.setDegreeName(entity.getDegreeName());
        dto.setInstitution(entity.getInstitution());
        dto.setYearObtained(entity.getYearObtained());
        if (entity.getDoctor() != null) {
            dto.setDoctorId(entity.getDoctor().getUserId());
        }
        return dto;
    }

    public DoctorDegree toDoctorDegreeEntity(DoctorDegreeRequestDTO request, Doctor doctor) {
        if (request == null) return null;
        return DoctorDegree.builder()
                .degreeName(request.getDegreeName())
                .institution(request.getInstitution())
                .yearObtained(request.getYearObtained())
                .doctor(doctor)
                .build();
    }

    public void updateDoctorDegreeEntity(DoctorDegree entity, DoctorDegreeRequestDTO request, Doctor doctor) {
        if (entity == null || request == null) return;
        entity.setDegreeName(request.getDegreeName());
        entity.setInstitution(request.getInstitution());
        entity.setYearObtained(request.getYearObtained());
        entity.setDoctor(doctor);
    }

    // WorkSchedule
    public WorkScheduleResponseDTO toWorkScheduleResponse(WorkSchedule entity) {
        if (entity == null) return null;
        WorkScheduleResponseDTO dto = new WorkScheduleResponseDTO();
        dto.setId(entity.getId());
        dto.setWorkDate(entity.getWorkDate());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        return dto;
    }

    public WorkSchedule toWorkScheduleEntity(WorkScheduleRequestDTO request) {
        if (request == null) return null;
        return WorkSchedule.builder()
                .workDate(request.getWorkDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();
    }

    public void updateWorkScheduleEntity(WorkSchedule entity, WorkScheduleRequestDTO request) {
        if (entity == null || request == null) return;
        entity.setWorkDate(request.getWorkDate());
        entity.setStartTime(request.getStartTime());
        entity.setEndTime(request.getEndTime());
    }

    // DoctorWorkSchedule
    public DoctorWorkScheduleResponseDTO toDoctorWorkScheduleResponse(DoctorWorkSchedule entity) {
        if (entity == null) return null;
        DoctorWorkScheduleResponseDTO dto = new DoctorWorkScheduleResponseDTO();
        dto.setId(entity.getId());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        if (entity.getWorkSchedule() != null) {
            dto.setWorkScheduleId(entity.getWorkSchedule().getId());
        }
        if (entity.getDoctor() != null) {
            dto.setDoctorId(entity.getDoctor().getUserId());
        }
        return dto;
    }

    public DoctorWorkSchedule toDoctorWorkScheduleEntity(DoctorWorkScheduleRequestDTO request, Doctor doctor, WorkSchedule workSchedule) {
        if (request == null) return null;
        return DoctorWorkSchedule.builder()
                .status(request.getStatus())
                .doctor(doctor)
                .workSchedule(workSchedule)
                .build();
    }

    public void updateDoctorWorkScheduleEntity(DoctorWorkSchedule entity, DoctorWorkScheduleRequestDTO request, Doctor doctor, WorkSchedule workSchedule) {
        if (entity == null || request == null) return;
        entity.setStatus(request.getStatus());
        entity.setDoctor(doctor);
        entity.setWorkSchedule(workSchedule);
    }
}
