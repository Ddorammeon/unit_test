package com.main_project.doctor_service.service;

import com.main_project.doctor_service.dto.DoctorDegreeRequestDTO;
import com.main_project.doctor_service.dto.DoctorRequestDTO;
import com.main_project.doctor_service.dto.DoctorResponseDTO;
import com.main_project.doctor_service.entity.Doctor;
import com.main_project.doctor_service.enums.SpecializationCodeEnum;
import com.main_project.doctor_service.repository.DoctorRepository;
import com.main_project.doctor_service.util.EntityMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

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
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private DoctorService doctorService;

    @Test
    @DisplayName("DOC-SRV-UT-001 - Create doctor successfully when user does not exist")
    void createDoctorShouldSaveDoctorWhenUserDoesNotExist() {
        // Note: DOC-SRV-UT-001 | Objective: create doctor successfully when the target user has no doctor record yet.
        UUID userId = UUID.randomUUID();
        DoctorRequestDTO request = buildDoctorRequest(userId);
        Doctor doctor = Doctor.builder().userId(userId).build();
        Doctor savedDoctor = Doctor.builder().userId(userId).build();
        DoctorResponseDTO expectedResponse = buildDoctorResponse(userId);

        when(doctorRepository.existsById(userId)).thenReturn(false);
        when(mapper.toDoctorEntity(request)).thenReturn(doctor);
        when(doctorRepository.save(doctor)).thenReturn(savedDoctor);
        when(mapper.toDoctorResponse(savedDoctor)).thenReturn(expectedResponse);

        DoctorResponseDTO actualResponse = doctorService.createDoctor(request);

        assertThat(actualResponse).isSameAs(expectedResponse);
        verify(doctorRepository).existsById(userId);
        verify(mapper).toDoctorEntity(request);
        verify(doctorRepository).save(doctor);
        verify(mapper).toDoctorResponse(savedDoctor);
    }

    @Test
    @DisplayName("DOC-SRV-UT-002 - Reject duplicate doctor creation")
    void createDoctorShouldThrowExceptionWhenDoctorAlreadyExists() {
        // Note: DOC-SRV-UT-002 | Objective: throw duplicate-data exception when doctor already exists for the user.
        UUID userId = UUID.randomUUID();
        DoctorRequestDTO request = buildDoctorRequest(userId);

        when(doctorRepository.existsById(userId)).thenReturn(true);

        assertThatThrownBy(() -> doctorService.createDoctor(request))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining(userId.toString());

        verify(doctorRepository).existsById(userId);
        verify(mapper, never()).toDoctorEntity(any());
        verify(doctorRepository, never()).save(any());
    }

    @Test
    @DisplayName("DOC-SRV-UT-003 - Return all mapped doctors")
    void getAllDoctorsShouldMapAllRepositoryResults() {
        // Note: DOC-SRV-UT-003 | Objective: return all doctors from repository after mapping each entity to DTO.
        Doctor firstDoctor = Doctor.builder().userId(UUID.randomUUID()).build();
        Doctor secondDoctor = Doctor.builder().userId(UUID.randomUUID()).build();
        DoctorResponseDTO firstResponse = buildDoctorResponse(firstDoctor.getUserId());
        DoctorResponseDTO secondResponse = buildDoctorResponse(secondDoctor.getUserId());

        when(doctorRepository.findAll()).thenReturn(List.of(firstDoctor, secondDoctor));
        when(mapper.toDoctorResponse(firstDoctor)).thenReturn(firstResponse);
        when(mapper.toDoctorResponse(secondDoctor)).thenReturn(secondResponse);

        List<DoctorResponseDTO> actualResponses = doctorService.getAllDoctors();

        assertThat(actualResponses).containsExactly(firstResponse, secondResponse);
        verify(doctorRepository).findAll();
    }

    @Test
    @DisplayName("DOC-SRV-UT-004 - Return doctor by id when found")
    void getDoctorByIdShouldReturnMappedDoctorWhenFound() {
        // Note: DOC-SRV-UT-004 | Objective: return the mapped doctor DTO when repository finds the doctor by id.
        UUID userId = UUID.randomUUID();
        Doctor doctor = Doctor.builder().userId(userId).build();
        DoctorResponseDTO expectedResponse = buildDoctorResponse(userId);

        when(doctorRepository.findById(userId)).thenReturn(Optional.of(doctor));
        when(mapper.toDoctorResponse(doctor)).thenReturn(expectedResponse);

        DoctorResponseDTO actualResponse = doctorService.getDoctorById(userId);

        assertThat(actualResponse).isSameAs(expectedResponse);
        verify(doctorRepository).findById(userId);
        verify(mapper).toDoctorResponse(doctor);
    }

    @Test
    @DisplayName("DOC-SRV-UT-005 - Throw when doctor id is missing")
    void getDoctorByIdShouldThrowExceptionWhenDoctorIsMissing() {
        // Note: DOC-SRV-UT-005 | Objective: throw not-found exception when requested doctor id does not exist.
        UUID userId = UUID.randomUUID();
        when(doctorRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.getDoctorById(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

    @Test
    @DisplayName("DOC-SRV-UT-006 - Update doctor successfully")
    void updateDoctorShouldSaveUpdatedDoctorWhenDoctorExists() {
        // Note: DOC-SRV-UT-006 | Objective: update an existing doctor, persist changes, and return mapped response.
        UUID userId = UUID.randomUUID();
        DoctorRequestDTO request = buildDoctorRequest(userId);
        Doctor existingDoctor = Doctor.builder().userId(userId).build();
        DoctorResponseDTO expectedResponse = buildDoctorResponse(userId);

        when(doctorRepository.findById(userId)).thenReturn(Optional.of(existingDoctor));
        when(doctorRepository.save(existingDoctor)).thenReturn(existingDoctor);
        when(mapper.toDoctorResponse(existingDoctor)).thenReturn(expectedResponse);

        DoctorResponseDTO actualResponse = doctorService.updateDoctor(userId, request);

        assertThat(actualResponse).isSameAs(expectedResponse);
        verify(doctorRepository).findById(userId);
        verify(mapper).updateDoctorEntity(existingDoctor, request);
        verify(doctorRepository).save(existingDoctor);
        verify(mapper).toDoctorResponse(existingDoctor);
    }

    @Test
    @DisplayName("DOC-SRV-UT-007 - Reject update for missing doctor")
    void updateDoctorShouldThrowExceptionWhenDoctorDoesNotExist() {
        // Note: DOC-SRV-UT-007 | Objective: throw not-found exception when updating a doctor that does not exist.
        UUID userId = UUID.randomUUID();
        DoctorRequestDTO request = buildDoctorRequest(userId);
        when(doctorRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.updateDoctor(userId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(userId.toString());

        verify(mapper, never()).updateDoctorEntity(any(), any());
        verify(doctorRepository, never()).save(any());
    }

    @Test
    @DisplayName("DOC-SRV-UT-008 - Delete doctor successfully")
    void deleteDoctorShouldDeleteDoctorWhenDoctorExists() {
        // Note: DOC-SRV-UT-008 | Objective: delete an existing doctor record after existence check passes.
        UUID userId = UUID.randomUUID();
        when(doctorRepository.existsById(userId)).thenReturn(true);

        doctorService.deleteDoctor(userId);

        verify(doctorRepository).existsById(userId);
        verify(doctorRepository).deleteById(userId);
    }

    @Test
    @DisplayName("DOC-SRV-UT-009 - Reject delete for missing doctor")
    void deleteDoctorShouldThrowExceptionWhenDoctorDoesNotExist() {
        // Note: DOC-SRV-UT-009 | Objective: throw not-found exception when deleting a doctor that does not exist.
        UUID userId = UUID.randomUUID();
        when(doctorRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> doctorService.deleteDoctor(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(userId.toString());

        verify(doctorRepository).existsById(userId);
        verify(doctorRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("DOC-SRV-UT-010 - updateDoctor should preserve existing degrees when degrees are omitted")
    void updateDoctorShouldPreserveExistingDegreesWhenDegreesAreOmitted() {
        // Note: DOC-SRV-UT-010 | Objective: ensure updating basic info only does not silently remove existing doctor degrees.
        DoctorService realMapperDoctorService = new DoctorService(doctorRepository, new EntityMapper());
        UUID userId = UUID.randomUUID();
        Doctor existingDoctor = Doctor.builder()
                .userId(userId)
                .specializationCode(SpecializationCodeEnum.ORTHO)
                .workingHospital("Old Hospital")
                .licenseNumber("LIC-001")
                .consultationFeeAmount(500000)
                .degrees(new java.util.ArrayList<>())
                .build();
        existingDoctor.addDegree("DDS", "Medical University", 2020);

        DoctorRequestDTO request = new DoctorRequestDTO();
        request.setUserId(userId);
        request.setSpecializationCode(SpecializationCodeEnum.ENDO);
        request.setWorkingHospital("New Hospital");
        request.setLicenseNumber("LIC-NEW");
        request.setConsultationFeeAmount(700000);
        request.setDegrees(null);

        when(doctorRepository.findById(userId)).thenReturn(Optional.of(existingDoctor));
        when(doctorRepository.save(existingDoctor)).thenReturn(existingDoctor);

        DoctorResponseDTO response = realMapperDoctorService.updateDoctor(userId, request);

        assertThat(response.getDegrees()).hasSize(1);
    }

    private DoctorRequestDTO buildDoctorRequest(UUID userId) {
        DoctorDegreeRequestDTO degreeRequest = new DoctorDegreeRequestDTO();
        degreeRequest.setDoctorId(userId);
        degreeRequest.setDegreeName("DDS");
        degreeRequest.setInstitution("HCM Medical University");
        degreeRequest.setYearObtained(2020);

        DoctorRequestDTO request = new DoctorRequestDTO();
        request.setUserId(userId);
        request.setSpecializationCode(SpecializationCodeEnum.ORTHO);
        request.setWorkingHospital("City Hospital");
        request.setLicenseNumber("LIC-001");
        request.setConsultationFeeAmount(500000);
        request.setDegrees(List.of(degreeRequest));
        return request;
    }

    private DoctorResponseDTO buildDoctorResponse(UUID userId) {
        DoctorResponseDTO response = new DoctorResponseDTO();
        response.setUserId(userId);
        response.setSpecializationCode(SpecializationCodeEnum.ORTHO);
        response.setWorkingHospital("City Hospital");
        response.setLicenseNumber("LIC-001");
        response.setConsultationFeeAmount(500000);
        return response;
    }
}
