package com.main_project.doctor_service.service;

import com.main_project.doctor_service.dto.DoctorDegreeRequestDTO;
import com.main_project.doctor_service.dto.DoctorDegreeResponseDTO;
import com.main_project.doctor_service.entity.Doctor;
import com.main_project.doctor_service.entity.DoctorDegree;
import com.main_project.doctor_service.repository.DoctorDegreeRepository;
import com.main_project.doctor_service.repository.DoctorRepository;
import com.main_project.doctor_service.util.EntityMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class DoctorDegreeServiceTest {

    @Mock
    private DoctorDegreeRepository doctorDegreeRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private DoctorDegreeService doctorDegreeService;

    @Test
    @DisplayName("DOC-DEG-UT-001 - Create degree successfully")
    void createDoctorDegreeShouldSaveDegreeWhenDoctorExists() {
        // Note: DOC-DEG-UT-001 | Objective: create a doctor degree successfully when the owning doctor exists.
        UUID doctorId = UUID.randomUUID();
        DoctorDegreeRequestDTO request = buildDegreeRequest(doctorId);
        Doctor doctor = Doctor.builder().userId(doctorId).build();
        DoctorDegree degree = DoctorDegree.builder().doctor(doctor).build();
        DoctorDegreeResponseDTO expectedResponse = buildDegreeResponse(doctorId);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(mapper.toDoctorDegreeEntity(request, doctor)).thenReturn(degree);
        when(doctorDegreeRepository.save(degree)).thenReturn(degree);
        when(mapper.toDoctorDegreeResponse(degree)).thenReturn(expectedResponse);

        DoctorDegreeResponseDTO actualResponse = doctorDegreeService.createDoctorDegree(request);

        assertThat(actualResponse).isSameAs(expectedResponse);
        verify(doctorRepository).findById(doctorId);
        verify(mapper).toDoctorDegreeEntity(request, doctor);
        verify(doctorDegreeRepository).save(degree);
    }

    @Test
    @DisplayName("DOC-DEG-UT-002 - Reject degree creation when doctor missing")
    void createDoctorDegreeShouldThrowExceptionWhenDoctorDoesNotExist() {
        // Note: DOC-DEG-UT-002 | Objective: throw not-found exception when creating a degree for a missing doctor.
        UUID doctorId = UUID.randomUUID();
        DoctorDegreeRequestDTO request = buildDegreeRequest(doctorId);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorDegreeService.createDoctorDegree(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(doctorId.toString());

        verify(mapper, never()).toDoctorDegreeEntity(any(), any());
        verify(doctorDegreeRepository, never()).save(any());
    }

    @Test
    @DisplayName("DOC-DEG-UT-003 - Return all mapped degrees")
    void getAllDoctorDegreesShouldMapAllResults() {
        // Note: DOC-DEG-UT-003 | Objective: return all degree records after mapping repository entities to DTOs.
        DoctorDegree firstDegree = DoctorDegree.builder().id(UUID.randomUUID()).build();
        DoctorDegree secondDegree = DoctorDegree.builder().id(UUID.randomUUID()).build();
        DoctorDegreeResponseDTO firstResponse = buildDegreeResponse(UUID.randomUUID());
        DoctorDegreeResponseDTO secondResponse = buildDegreeResponse(UUID.randomUUID());

        when(doctorDegreeRepository.findAll()).thenReturn(List.of(firstDegree, secondDegree));
        when(mapper.toDoctorDegreeResponse(firstDegree)).thenReturn(firstResponse);
        when(mapper.toDoctorDegreeResponse(secondDegree)).thenReturn(secondResponse);

        List<DoctorDegreeResponseDTO> actualResponses = doctorDegreeService.getAllDoctorDegrees();

        assertThat(actualResponses).containsExactly(firstResponse, secondResponse);
        verify(doctorDegreeRepository).findAll();
    }

    @Test
    @DisplayName("DOC-DEG-UT-004 - Return degree by id when found")
    void getDoctorDegreeByIdShouldReturnMappedResultWhenFound() {
        // Note: DOC-DEG-UT-004 | Objective: return the mapped degree DTO when repository finds the degree by id.
        UUID degreeId = UUID.randomUUID();
        DoctorDegree degree = DoctorDegree.builder().id(degreeId).build();
        DoctorDegreeResponseDTO expectedResponse = buildDegreeResponse(UUID.randomUUID());

        when(doctorDegreeRepository.findById(degreeId)).thenReturn(Optional.of(degree));
        when(mapper.toDoctorDegreeResponse(degree)).thenReturn(expectedResponse);

        DoctorDegreeResponseDTO actualResponse = doctorDegreeService.getDoctorDegreeById(degreeId);

        assertThat(actualResponse).isSameAs(expectedResponse);
    }

    @Test
    @DisplayName("DOC-DEG-UT-005 - Throw when degree id is missing")
    void getDoctorDegreeByIdShouldThrowExceptionWhenMissing() {
        // Note: DOC-DEG-UT-005 | Objective: throw not-found exception when requested degree id does not exist.
        UUID degreeId = UUID.randomUUID();
        when(doctorDegreeRepository.findById(degreeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorDegreeService.getDoctorDegreeById(degreeId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(degreeId.toString());
    }

    @Test
    @DisplayName("DOC-DEG-UT-006 - Return degrees by doctor id")
    void getDegreesByDoctorIdShouldMapDoctorDegrees() {
        // Note: DOC-DEG-UT-006 | Objective: return all degree DTOs associated with a given doctor id.
        UUID doctorId = UUID.randomUUID();
        DoctorDegree degree = DoctorDegree.builder().id(UUID.randomUUID()).build();
        DoctorDegreeResponseDTO expectedResponse = buildDegreeResponse(doctorId);

        when(doctorDegreeRepository.findByDoctor_UserId(doctorId)).thenReturn(List.of(degree));
        when(mapper.toDoctorDegreeResponse(degree)).thenReturn(expectedResponse);

        List<DoctorDegreeResponseDTO> actualResponses = doctorDegreeService.getDegreesByDoctorId(doctorId);

        assertThat(actualResponses).containsExactly(expectedResponse);
    }

    @Test
    @DisplayName("DOC-DEG-UT-007 - Update degree successfully")
    void updateDoctorDegreeShouldSaveUpdatedDegreeWhenDataExists() {
        // Note: DOC-DEG-UT-007 | Objective: update an existing degree successfully when both degree and doctor exist.
        UUID degreeId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        DoctorDegreeRequestDTO request = buildDegreeRequest(doctorId);
        DoctorDegree existingDegree = DoctorDegree.builder().id(degreeId).build();
        Doctor doctor = Doctor.builder().userId(doctorId).build();
        DoctorDegreeResponseDTO expectedResponse = buildDegreeResponse(doctorId);

        when(doctorDegreeRepository.findById(degreeId)).thenReturn(Optional.of(existingDegree));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorDegreeRepository.save(existingDegree)).thenReturn(existingDegree);
        when(mapper.toDoctorDegreeResponse(existingDegree)).thenReturn(expectedResponse);

        DoctorDegreeResponseDTO actualResponse = doctorDegreeService.updateDoctorDegree(degreeId, request);

        assertThat(actualResponse).isSameAs(expectedResponse);
        verify(mapper).updateDoctorDegreeEntity(existingDegree, request, doctor);
    }

    @Test
    @DisplayName("DOC-DEG-UT-008 - Reject update for missing degree")
    void updateDoctorDegreeShouldThrowExceptionWhenDegreeMissing() {
        // Note: DOC-DEG-UT-008 | Objective: throw not-found exception when updating a degree that does not exist.
        UUID degreeId = UUID.randomUUID();
        DoctorDegreeRequestDTO request = buildDegreeRequest(UUID.randomUUID());
        when(doctorDegreeRepository.findById(degreeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorDegreeService.updateDoctorDegree(degreeId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(degreeId.toString());

        verify(doctorRepository, never()).findById(any());
    }

    @Test
    @DisplayName("DOC-DEG-UT-009 - Delete degree successfully")
    void deleteDoctorDegreeShouldDeleteWhenDegreeExists() {
        // Note: DOC-DEG-UT-009 | Objective: delete an existing degree after existence check passes.
        UUID degreeId = UUID.randomUUID();
        when(doctorDegreeRepository.existsById(degreeId)).thenReturn(true);

        doctorDegreeService.deleteDoctorDegree(degreeId);

        verify(doctorDegreeRepository).deleteById(degreeId);
    }

    @Test
    @DisplayName("DOC-DEG-UT-010 - Reject delete for missing degree")
    void deleteDoctorDegreeShouldThrowExceptionWhenDegreeMissing() {
        // Note: DOC-DEG-UT-010 | Objective: throw not-found exception when deleting a degree that does not exist.
        UUID degreeId = UUID.randomUUID();
        when(doctorDegreeRepository.existsById(degreeId)).thenReturn(false);

        assertThatThrownBy(() -> doctorDegreeService.deleteDoctorDegree(degreeId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(degreeId.toString());

        verify(doctorDegreeRepository, never()).deleteById(any());
    }

    private DoctorDegreeRequestDTO buildDegreeRequest(UUID doctorId) {
        DoctorDegreeRequestDTO request = new DoctorDegreeRequestDTO();
        request.setDoctorId(doctorId);
        request.setDegreeName("DDS");
        request.setInstitution("Medical University");
        request.setYearObtained(2020);
        return request;
    }

    private DoctorDegreeResponseDTO buildDegreeResponse(UUID doctorId) {
        DoctorDegreeResponseDTO response = new DoctorDegreeResponseDTO();
        response.setId(UUID.randomUUID());
        response.setDoctorId(doctorId);
        response.setDegreeName("DDS");
        response.setInstitution("Medical University");
        response.setYearObtained(2020);
        return response;
    }
}
