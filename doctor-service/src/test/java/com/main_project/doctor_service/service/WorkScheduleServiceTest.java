package com.main_project.doctor_service.service;

import com.main_project.doctor_service.dto.WorkScheduleRequestDTO;
import com.main_project.doctor_service.dto.WorkScheduleResponseDTO;
import com.main_project.doctor_service.entity.WorkSchedule;
import com.main_project.doctor_service.repository.WorkScheduleRepository;
import com.main_project.doctor_service.util.EntityMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneOffset;
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
class WorkScheduleServiceTest {

    @Mock
    private WorkScheduleRepository workScheduleRepository;

    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private WorkScheduleService workScheduleService;

    @Test
    @DisplayName("WORK-SRV-UT-001 - Create work schedule successfully")
    void createWorkScheduleShouldSaveMappedSchedule() {
        // Note: WORK-SRV-UT-001 | Objective: create a work schedule successfully from a valid request DTO.
        WorkScheduleRequestDTO request = buildWorkScheduleRequest();
        WorkSchedule schedule = WorkSchedule.builder().build();
        WorkSchedule savedSchedule = WorkSchedule.builder().id(UUID.randomUUID()).build();
        WorkScheduleResponseDTO expectedResponse = buildWorkScheduleResponse(savedSchedule.getId());

        when(mapper.toWorkScheduleEntity(request)).thenReturn(schedule);
        when(workScheduleRepository.save(schedule)).thenReturn(savedSchedule);
        when(mapper.toWorkScheduleResponse(savedSchedule)).thenReturn(expectedResponse);

        WorkScheduleResponseDTO actualResponse = workScheduleService.createWorkSchedule(request);

        assertThat(actualResponse).isSameAs(expectedResponse);
        verify(mapper).toWorkScheduleEntity(request);
        verify(workScheduleRepository).save(schedule);
    }

    @Test
    @DisplayName("WORK-SRV-UT-002 - Return all mapped work schedules")
    void getAllWorkSchedulesShouldMapAllResults() {
        // Note: WORK-SRV-UT-002 | Objective: return all work schedule DTOs after mapping repository entities.
        WorkSchedule firstSchedule = WorkSchedule.builder().id(UUID.randomUUID()).build();
        WorkSchedule secondSchedule = WorkSchedule.builder().id(UUID.randomUUID()).build();
        WorkScheduleResponseDTO firstResponse = buildWorkScheduleResponse(firstSchedule.getId());
        WorkScheduleResponseDTO secondResponse = buildWorkScheduleResponse(secondSchedule.getId());

        when(workScheduleRepository.findAll()).thenReturn(List.of(firstSchedule, secondSchedule));
        when(mapper.toWorkScheduleResponse(firstSchedule)).thenReturn(firstResponse);
        when(mapper.toWorkScheduleResponse(secondSchedule)).thenReturn(secondResponse);

        List<WorkScheduleResponseDTO> actualResponses = workScheduleService.getAllWorkSchedules();

        assertThat(actualResponses).containsExactly(firstResponse, secondResponse);
    }

    @Test
    @DisplayName("WORK-SRV-UT-003 - Return work schedule by id when found")
    void getWorkScheduleByIdShouldReturnMappedScheduleWhenFound() {
        // Note: WORK-SRV-UT-003 | Objective: return the mapped work schedule DTO when repository finds the id.
        UUID id = UUID.randomUUID();
        WorkSchedule schedule = WorkSchedule.builder().id(id).build();
        WorkScheduleResponseDTO expectedResponse = buildWorkScheduleResponse(id);

        when(workScheduleRepository.findById(id)).thenReturn(Optional.of(schedule));
        when(mapper.toWorkScheduleResponse(schedule)).thenReturn(expectedResponse);

        WorkScheduleResponseDTO actualResponse = workScheduleService.getWorkScheduleById(id);

        assertThat(actualResponse).isSameAs(expectedResponse);
    }

    @Test
    @DisplayName("WORK-SRV-UT-004 - Throw when work schedule id is missing")
    void getWorkScheduleByIdShouldThrowExceptionWhenMissing() {
        // Note: WORK-SRV-UT-004 | Objective: throw not-found exception when requested work schedule id does not exist.
        UUID id = UUID.randomUUID();
        when(workScheduleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workScheduleService.getWorkScheduleById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("WORK-SRV-UT-005 - Update work schedule successfully")
    void updateWorkScheduleShouldSaveUpdatedScheduleWhenFound() {
        // Note: WORK-SRV-UT-005 | Objective: update an existing work schedule and return the mapped result.
        UUID id = UUID.randomUUID();
        WorkScheduleRequestDTO request = buildWorkScheduleRequest();
        WorkSchedule existingSchedule = WorkSchedule.builder().id(id).build();
        WorkScheduleResponseDTO expectedResponse = buildWorkScheduleResponse(id);

        when(workScheduleRepository.findById(id)).thenReturn(Optional.of(existingSchedule));
        when(workScheduleRepository.save(existingSchedule)).thenReturn(existingSchedule);
        when(mapper.toWorkScheduleResponse(existingSchedule)).thenReturn(expectedResponse);

        WorkScheduleResponseDTO actualResponse = workScheduleService.updateWorkSchedule(id, request);

        assertThat(actualResponse).isSameAs(expectedResponse);
        verify(mapper).updateWorkScheduleEntity(existingSchedule, request);
    }

    @Test
    @DisplayName("WORK-SRV-UT-006 - Reject update for missing work schedule")
    void updateWorkScheduleShouldThrowExceptionWhenMissing() {
        // Note: WORK-SRV-UT-006 | Objective: throw not-found exception when updating a missing work schedule.
        UUID id = UUID.randomUUID();
        WorkScheduleRequestDTO request = buildWorkScheduleRequest();
        when(workScheduleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workScheduleService.updateWorkSchedule(id, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(mapper, never()).updateWorkScheduleEntity(any(), any());
    }

    @Test
    @DisplayName("WORK-SRV-UT-007 - Delete work schedule successfully")
    void deleteWorkScheduleShouldDeleteWhenScheduleExists() {
        // Note: WORK-SRV-UT-007 | Objective: delete an existing work schedule after existence check succeeds.
        UUID id = UUID.randomUUID();
        when(workScheduleRepository.existsById(id)).thenReturn(true);

        workScheduleService.deleteWorkSchedule(id);

        verify(workScheduleRepository).deleteById(id);
    }

    @Test
    @DisplayName("WORK-SRV-UT-008 - Reject delete for missing work schedule")
    void deleteWorkScheduleShouldThrowExceptionWhenScheduleMissing() {
        // Note: WORK-SRV-UT-008 | Objective: throw not-found exception when deleting a missing work schedule.
        UUID id = UUID.randomUUID();
        when(workScheduleRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> workScheduleService.deleteWorkSchedule(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    private WorkScheduleRequestDTO buildWorkScheduleRequest() {
        WorkScheduleRequestDTO request = new WorkScheduleRequestDTO();
        request.setWorkDate(LocalDate.of(2026, 5, 13));
        request.setStartTime(ZonedDateTime.of(2026, 5, 13, 8, 0, 0, 0, ZoneOffset.UTC));
        request.setEndTime(ZonedDateTime.of(2026, 5, 13, 17, 0, 0, 0, ZoneOffset.UTC));
        return request;
    }

    private WorkScheduleResponseDTO buildWorkScheduleResponse(UUID id) {
        WorkScheduleResponseDTO response = new WorkScheduleResponseDTO();
        response.setId(id);
        response.setWorkDate(LocalDate.of(2026, 5, 13));
        response.setStartTime(ZonedDateTime.of(2026, 5, 13, 8, 0, 0, 0, ZoneOffset.UTC));
        response.setEndTime(ZonedDateTime.of(2026, 5, 13, 17, 0, 0, 0, ZoneOffset.UTC));
        return response;
    }
}
