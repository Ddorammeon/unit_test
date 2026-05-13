package com.main_project.appointment_service.util;

import com.main_project.appointment_service.dto.*;
import com.main_project.appointment_service.entity.Appointment;
import com.main_project.appointment_service.entity.MedicalService;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
public class EntityDTOMapper {

    // ========================
    // Appointment -> DTO
    // ========================
    public AppointmentDTO toAppointmentDTO(Appointment entity) {
        if (entity == null) return null;

        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(entity.getId());
        dto.setDoctorId(entity.getDoctorId());
        dto.setPatientId(entity.getPatientId());
        dto.setAppointmentStartTime(entity.getAppointmentStartTime());
        dto.setAppointmentEndTime(entity.getAppointmentEndTime());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getMedicalServices() != null && !entity.getMedicalServices().isEmpty()) {
            List<MedicalServiceDTO> serviceDTOs = entity.getMedicalServices().stream()
                    .map(ms -> {
                        MedicalServiceDTO msDTO = new MedicalServiceDTO();
                        msDTO.setId(ms.getId());
                        msDTO.setServiceName(ms.getServiceName());
                        msDTO.setServiceType(ms.getServiceType());
                        msDTO.setServiceTime(ms.getServiceTime());
                        msDTO.setStatus(ms.getStatus());
                        msDTO.setPrice(ms.getPrice());
                        msDTO.setDescription(ms.getDescription());
                        msDTO.setImgUrl(ms.getImgUrl());
                        return msDTO;
                    }).toList();
            dto.setMedicalServices(serviceDTOs);
        } else {
            dto.setMedicalServices(List.of());
        }

        return dto;
    }

    // ========================
    // RequestDTO -> Appointment Entity
    // ========================
    public Appointment toAppointmentEntity(AppointmentRequestDTO requestDTO, List<MedicalService> medicalServices) {
        if (requestDTO == null) return null;

        Appointment entity = new Appointment();
        entity.setDoctorId(requestDTO.getDoctorId());
        entity.setPatientId(requestDTO.getPatientId());
        entity.setAppointmentStartTime(requestDTO.getAppointmentStartTime());
        entity.setAppointmentEndTime(requestDTO.getAppointmentEndTime());
        entity.setStatus(requestDTO.getStatus());
        entity.setCreatedAt(ZonedDateTime.now());
        entity.setUpdatedAt(ZonedDateTime.now());

        entity.setMedicalServices(medicalServices);
        return entity;
    }

    // ========================
    // Update Entity from RequestDTO
    // ========================
    public void updateAppointmentEntity(Appointment entity, AppointmentRequestDTO requestDTO, List<MedicalService> medicalServices) {
        if (entity == null || requestDTO == null) return;

        entity.setDoctorId(requestDTO.getDoctorId());
        entity.setPatientId(requestDTO.getPatientId());
        entity.setAppointmentStartTime(requestDTO.getAppointmentStartTime());
        entity.setAppointmentEndTime(requestDTO.getAppointmentEndTime());
        entity.setStatus(requestDTO.getStatus());
        entity.setUpdatedAt(ZonedDateTime.now());

        if (medicalServices != null) {
            entity.setMedicalServices(medicalServices);
        } else {
            entity.setMedicalServices(List.of());
        }
    }

    // ======================
    // MedicalService Mapping
    // ======================

    public MedicalServiceDTO toMedicalServiceDTO(MedicalService entity) {
        if (entity == null) return null;

        MedicalServiceDTO dto = new MedicalServiceDTO();
        dto.setId(entity.getId());
        dto.setServiceName(entity.getServiceName());
        dto.setServiceType(entity.getServiceType());
        dto.setServiceTime(entity.getServiceTime());
        dto.setStatus(entity.getStatus());
        dto.setPrice(entity.getPrice());
        dto.setDescription(entity.getDescription());
        dto.setImgUrl(entity.getImgUrl());
        return dto;
    }

    public MedicalService toMedicalServiceEntity(MedicalServiceRequestDTO requestDTO) {
        if (requestDTO == null) return null;

        MedicalService entity = new MedicalService();
        entity.setServiceName(requestDTO.getServiceName());
        entity.setServiceType(requestDTO.getServiceType());
        entity.setServiceTime(requestDTO.getServiceTime());
        entity.setStatus(requestDTO.getStatus());
        entity.setPrice(requestDTO.getPrice());
        entity.setDescription(requestDTO.getDescription());
        entity.setImgUrl(requestDTO.getImgUrl());
        return entity;
    }

    public void updateMedicalServiceEntity(MedicalService entity, MedicalServiceRequestDTO requestDTO) {
        if (entity == null || requestDTO == null) return;

        entity.setServiceName(requestDTO.getServiceName());
        entity.setServiceType(requestDTO.getServiceType());
        entity.setServiceTime(requestDTO.getServiceTime());
        entity.setStatus(requestDTO.getStatus());
        entity.setPrice(requestDTO.getPrice());
        entity.setDescription(requestDTO.getDescription());
        entity.setImgUrl(requestDTO.getImgUrl());
    }
}
