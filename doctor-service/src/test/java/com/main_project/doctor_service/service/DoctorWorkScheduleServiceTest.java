package com.main_project.doctor_service.service;

import com.main_project.doctor_service.dto.DoctorWorkScheduleRequestDTO;
import com.main_project.doctor_service.dto.DoctorWorkScheduleResponseDTO;
import com.main_project.doctor_service.entity.Doctor;
import com.main_project.doctor_service.entity.DoctorWorkSchedule;
import com.main_project.doctor_service.entity.WorkSchedule;
import com.main_project.doctor_service.repository.DoctorRepository;
import com.main_project.doctor_service.repository.DoctorWorkScheduleRepository;
import com.main_project.doctor_service.repository.WorkScheduleRepository;
import com.main_project.doctor_service.util.EntityMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorWorkScheduleServiceTest {

    @Mock
    private DoctorWorkScheduleRepository doctorWorkScheduleRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private WorkScheduleRepository workScheduleRepository;

    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private DoctorWorkScheduleService doctorWorkScheduleService;

    @Test
    @DisplayName("DOC-WS-UT-001 - Create doctor work schedule successfully")
    void createDoctorWorkScheduleShouldSaveWhenDoctorAndScheduleExist() {
        // Note: DOC-WS-UT-001 | Objective: create a doctor-work-schedule link when both doctor and work schedule exist.
        UUID doctorId = UUID.randomUUID();
        UUID workScheduleId = UUID.randomUUID();
        DoctorWorkScheduleRequestDTO request = buildDoctorWorkScheduleRequest(doctorId, workScheduleId);
        Doctor doctor = Doctor.builder().userId(doctorId).build();
        WorkSchedule workSchedule = WorkSchedule.builder().id(workScheduleId).build();
        DoctorWorkSchedule entity = DoctorWorkSchedule.builder().doctor(doctor).workSchedule(workSchedule).build();
        DoctorWorkScheduleResponseDTO expectedResponse = buildDoctorWorkScheduleResponse(doctorId, workScheduleId);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(workScheduleRepository.findById(workScheduleId)).thenReturn(Optional.of(workSchedule));
        when(mapper.toDoctorWorkScheduleEntity(request, doctor, workSchedule)).thenReturn(entity);
        when(doctorWorkScheduleRepository.save(entity)).thenReturn(entity);
        when(mapper.toDoctorWorkScheduleResponse(entity)).thenReturn(expectedResponse);

        DoctorWorkScheduleResponseDTO actualResponse = doctorWorkScheduleService.createDoctorWorkSchedule(request);

        assertThat(actualResponse).isSameAs(expectedResponse);
        verify(doctorRepository).findById(doctorId);
        verify(workScheduleRepository).findById(workScheduleId);
        verify(doctorWorkScheduleRepository).save(entity);
    }

    @Test
    @DisplayName("DOC-WS-UT-002 - Reject create when doctor missing")
    void createDoctorWorkScheduleShouldThrowExceptionWhenDoctorMissing() {
        // Note: DOC-WS-UT-002 | Objective: throw not-found exception when creating a link for a missing doctor.
        UUID doctorId = UUID.randomUUID();
        UUID workScheduleId = UUID.randomUUID();
        DoctorWorkScheduleRequestDTO request = buildDoctorWorkScheduleRequest(doctorId, workScheduleId);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorWorkScheduleService.createDoctorWorkSchedule(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(doctorId.toString());

        verify(workScheduleRepository, never()).findById(any());
    }

    @Test
    @DisplayName("DOC-WS-UT-003 - Return all mapped doctor work schedules")
    void getAllDoctorWorkSchedulesShouldMapAllResults() {
        // Note: DOC-WS-UT-003 | Objective: return all doctor-work-schedule DTOs after entity mapping.
        DoctorWorkSchedule firstEntity = DoctorWorkSchedule.builder().id(UUID.randomUUID()).build();
        DoctorWorkSchedule secondEntity = DoctorWorkSchedule.builder().id(UUID.randomUUID()).build();
        DoctorWorkScheduleResponseDTO firstResponse = buildDoctorWorkScheduleResponse(UUID.randomUUID(), UUID.randomUUID());
        DoctorWorkScheduleResponseDTO secondResponse = buildDoctorWorkScheduleResponse(UUID.randomUUID(), UUID.randomUUID());

        when(doctorWorkScheduleRepository.findAll()).thenReturn(List.of(firstEntity, secondEntity));
        when(mapper.toDoctorWorkScheduleResponse(firstEntity)).thenReturn(firstResponse);
        when(mapper.toDoctorWorkScheduleResponse(secondEntity)).thenReturn(secondResponse);

        List<DoctorWorkScheduleResponseDTO> actualResponses = doctorWorkScheduleService.getAllDoctorWorkSchedules();

        assertThat(actualResponses).containsExactly(firstResponse, secondResponse);
    }

    @Test
    @DisplayName("DOC-WS-UT-004 - Return doctor work schedule by id when found")
    void getDoctorWorkScheduleByIdShouldReturnMappedResultWhenFound() {
        // Note: DOC-WS-UT-004 | Objective: return the mapped link DTO when repository finds the record by id.
        UUID id = UUID.randomUUID();
        DoctorWorkSchedule entity = DoctorWorkSchedule.builder().id(id).build();
        DoctorWorkScheduleResponseDTO expectedResponse = buildDoctorWorkScheduleResponse(UUID.randomUUID(), UUID.randomUUID());

        when(doctorWorkScheduleRepository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDoctorWorkScheduleResponse(entity)).thenReturn(expectedResponse);

        DoctorWorkScheduleResponseDTO actualResponse = doctorWorkScheduleService.getDoctorWorkScheduleById(id);

        assertThat(actualResponse).isSameAs(expectedResponse);
    }

    @Test
    @DisplayName("DOC-WS-UT-005 - Throw when doctor work schedule id is missing")
    void getDoctorWorkScheduleByIdShouldThrowExceptionWhenMissing() {
        // Note: DOC-WS-UT-005 | Objective: throw not-found exception when requested link id does not exist.
        UUID id = UUID.randomUUID();
        when(doctorWorkScheduleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorWorkScheduleService.getDoctorWorkScheduleById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("DOC-WS-UT-006 - Return doctor work schedules by doctor id")
    void getDoctorWorkSchedulesByDoctorIdShouldMapResults() {
        // Note: DOC-WS-UT-006 | Objective: return all work schedule links for a doctor after mapping.
        UUID doctorId = UUID.randomUUID();
        DoctorWorkSchedule entity = DoctorWorkSchedule.builder().id(UUID.randomUUID()).build();
        DoctorWorkScheduleResponseDTO expectedResponse = buildDoctorWorkScheduleResponse(doctorId, UUID.randomUUID());

        when(doctorWorkScheduleRepository.findByDoctor_UserId(doctorId)).thenReturn(List.of(entity));
        when(mapper.toDoctorWorkScheduleResponse(entity)).thenReturn(expectedResponse);

        List<DoctorWorkScheduleResponseDTO> actualResponses = doctorWorkScheduleService.getDoctorWorkSchedulesByDoctorId(doctorId);

        assertThat(actualResponses).containsExactly(expectedResponse);
    }

    @Test
    @DisplayName("DOC-WS-UT-007 - Update doctor work schedule successfully")
    void updateDoctorWorkScheduleShouldSaveUpdatedEntityWhenDependenciesExist() {
        // Note: DOC-WS-UT-007 | Objective: update a doctor-work-schedule link when the link, doctor, and schedule all exist.
        UUID id = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID workScheduleId = UUID.randomUUID();
        DoctorWorkScheduleRequestDTO request = buildDoctorWorkScheduleRequest(doctorId, workScheduleId);
        DoctorWorkSchedule existingEntity = DoctorWorkSchedule.builder().id(id).build();
        Doctor doctor = Doctor.builder().userId(doctorId).build();
        WorkSchedule workSchedule = WorkSchedule.builder().id(workScheduleId).build();
        DoctorWorkScheduleResponseDTO expectedResponse = buildDoctorWorkScheduleResponse(doctorId, workScheduleId);

        when(doctorWorkScheduleRepository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(workScheduleRepository.findById(workScheduleId)).thenReturn(Optional.of(workSchedule));
        when(doctorWorkScheduleRepository.save(existingEntity)).thenReturn(existingEntity);
        when(mapper.toDoctorWorkScheduleResponse(existingEntity)).thenReturn(expectedResponse);

        DoctorWorkScheduleResponseDTO actualResponse = doctorWorkScheduleService.updateDoctorWorkSchedule(id, request);

        assertThat(actualResponse).isSameAs(expectedResponse);
        verify(mapper).updateDoctorWorkScheduleEntity(existingEntity, request, doctor, workSchedule);
    }

    @Test
    @DisplayName("DOC-WS-UT-008 - Delete doctor work schedule successfully")
    void deleteDoctorWorkScheduleShouldDeleteWhenEntityExists() {
        // Note: DOC-WS-UT-008 | Objective: delete an existing doctor-work-schedule link successfully.
        UUID id = UUID.randomUUID();
        when(doctorWorkScheduleRepository.existsById(id)).thenReturn(true);

        doctorWorkScheduleService.deleteDoctorWorkSchedule(id);

        verify(doctorWorkScheduleRepository).deleteById(id);
    }

    @Test
    @DisplayName("DOC-WS-UT-009 - Reject delete for missing doctor work schedule")
    void deleteDoctorWorkScheduleShouldThrowExceptionWhenEntityMissing() {
        // Note: DOC-WS-UT-009 | Objective: throw not-found exception when deleting a missing doctor-work-schedule link.
        UUID id = UUID.randomUUID();
        when(doctorWorkScheduleRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> doctorWorkScheduleService.deleteDoctorWorkSchedule(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    private DoctorWorkScheduleRequestDTO buildDoctorWorkScheduleRequest(UUID doctorId, UUID workScheduleId) {
        DoctorWorkScheduleRequestDTO request = new DoctorWorkScheduleRequestDTO();
        request.setDoctorId(doctorId);
        request.setWorkScheduleId(workScheduleId);
        request.setStatus("ACTIVE");
        return request;
    }

    private DoctorWorkScheduleResponseDTO buildDoctorWorkScheduleResponse(UUID doctorId, UUID workScheduleId) {
        DoctorWorkScheduleResponseDTO response = new DoctorWorkScheduleResponseDTO();
        response.setId(UUID.randomUUID());
        response.setDoctorId(doctorId);
        response.setWorkScheduleId(workScheduleId);
        response.setStatus("ACTIVE");
        response.setCreatedAt(ZonedDateTime.now());
        response.setUpdatedAt(ZonedDateTime.now());
        return response;
    }
}
