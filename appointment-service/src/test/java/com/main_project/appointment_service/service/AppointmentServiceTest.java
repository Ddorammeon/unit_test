package com.main_project.appointment_service.service;

import com.main_project.appointment_service.dto.AppointmentDTO;
import com.main_project.appointment_service.dto.AppointmentRequestDTO;
import com.main_project.appointment_service.dto.UserDTO;
import com.main_project.appointment_service.entity.Appointment;
import com.main_project.appointment_service.entity.MedicalService;
import com.main_project.appointment_service.enums.AppointmentStatus;
import com.main_project.appointment_service.enums.MedicalServiceStatus;
import com.main_project.appointment_service.exceptions.AppException;
import com.main_project.appointment_service.feignclient.UserServiceClient;
import com.main_project.appointment_service.repository.AppointmentRepository;
import com.main_project.appointment_service.repository.MedicalServiceRepository;
import com.main_project.appointment_service.util.EntityDTOMapper;
import jakarta.persistence.EntityNotFoundException;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.main_project.appointment_service.exceptions.enums.ErrorCode.APPINTMENT_IS_NOT_CHECKIN_YET;
import static com.main_project.appointment_service.exceptions.enums.ErrorCode.APPOINTMENT_NOT_EXISTED;
import static com.main_project.appointment_service.exceptions.enums.ErrorCode.INVALID_MEDICAL_SERVICE_ID;
import static com.main_project.appointment_service.exceptions.enums.ErrorCode.LIST_MEDICAL_SERVICE_EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private UserServiceClient userServiceClient;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private MedicalServiceRepository medicalServiceRepository;
    @Mock
    private SlotService slotService;
    @Mock
    private EntityDTOMapper mapper;
    @Mock
    private EventBus eventBus;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    @DisplayName("APP-SRV-UT-011 - Reject create appointment when service list is empty")
    void createAppointmentShouldRejectEmptyMedicalServiceList() {
        // Note: APP-SRV-UT-011 | Objective: throw AppException when no medical service ids are provided during creation.
        AppointmentRequestDTO request = buildAppointmentRequest();
        request.setMedicalServiceIds(List.of());

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(LIST_MEDICAL_SERVICE_EMPTY);
    }

    @Test
    @DisplayName("APP-SRV-UT-012 - Reject create appointment when service ids are invalid")
    void createAppointmentShouldRejectInvalidMedicalServiceIds() {
        // Note: APP-SRV-UT-012 | Objective: throw AppException when at least one medical service id cannot be resolved.
        AppointmentRequestDTO request = buildAppointmentRequest();
        when(medicalServiceRepository.findAllById(request.getMedicalServiceIds())).thenReturn(List.of(buildMedicalService(request.getMedicalServiceIds().get(0), 20)));

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(INVALID_MEDICAL_SERVICE_ID);
    }

    @Test
    @DisplayName("APP-SRV-UT-013 - Reject create appointment when slot validation fails")
    void createAppointmentShouldRejectWhenSlotValidationFails() {
        // Note: APP-SRV-UT-013 | Objective: reject appointment creation when Redis slot validation/unlock returns false.
        AppointmentRequestDTO request = buildAppointmentRequest();
        List<MedicalService> services = List.of(
                buildMedicalService(request.getMedicalServiceIds().get(0), 20),
                buildMedicalService(request.getMedicalServiceIds().get(1), 10)
        );

        when(medicalServiceRepository.findAllById(request.getMedicalServiceIds())).thenReturn(services);
        when(slotService.validateAndUnlockSlot(any(), any(), any(), any())).thenReturn(false);

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Slot");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("APP-SRV-UT-014 - Reject create appointment when DB overlap exists")
    void createAppointmentShouldRejectWhenOverlappingAppointmentExists() {
        // Note: APP-SRV-UT-014 | Objective: reject appointment creation and unlock slot when overlapping DB records exist.
        AppointmentRequestDTO request = buildAppointmentRequest();
        List<MedicalService> services = List.of(
                buildMedicalService(request.getMedicalServiceIds().get(0), 20),
                buildMedicalService(request.getMedicalServiceIds().get(1), 10)
        );

        when(medicalServiceRepository.findAllById(request.getMedicalServiceIds())).thenReturn(services);
        when(slotService.validateAndUnlockSlot(any(), any(), any(), any())).thenReturn(true);
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any())).thenReturn(List.of(buildAppointment()));

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not available");

        verify(slotService, times(2)).unlockSlot(any(), any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("APP-SRV-UT-015 - Create appointment successfully")
    void createAppointmentShouldSavePublishEventAndReturnDto() {
        // Note: APP-SRV-UT-015 | Objective: create appointment successfully, persist it, unlock slot, and publish event.
        AppointmentRequestDTO request = buildAppointmentRequest();
        List<MedicalService> services = List.of(
                buildMedicalService(request.getMedicalServiceIds().get(0), 20),
                buildMedicalService(request.getMedicalServiceIds().get(1), 10)
        );
        Appointment appointment = buildAppointment();
        AppointmentDTO dto = buildAppointmentDto(appointment);

        when(medicalServiceRepository.findAllById(request.getMedicalServiceIds())).thenReturn(services);
        when(slotService.validateAndUnlockSlot(any(), any(), any(), any())).thenReturn(true);
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any())).thenReturn(List.of());
        when(mapper.toAppointmentEntity(request, services)).thenReturn(appointment);
        when(mapper.toAppointmentDTO(appointment)).thenReturn(dto);

        AppointmentDTO actual = appointmentService.createAppointment(request);

        assertThat(actual).isSameAs(dto);
        verify(appointmentRepository).save(appointment);
        verify(slotService).unlockSlot(request.getDoctorId(), request.getAppointmentStartTime());
        ArgumentCaptor<EventMessage<?>> eventCaptor = ArgumentCaptor.forClass(EventMessage.class);
        verify(eventBus).publish(eventCaptor.capture());
        assertThat(request.getAppointmentEndTime()).isEqualTo(request.getAppointmentStartTime().plusMinutes(30));
    }

    @Test
    @DisplayName("APP-SRV-UT-016 - Create appointment should validate doctor before saving")
    void createAppointmentShouldValidateDoctorBeforeSaving() {
        // Note: APP-SRV-UT-016 | Objective: require doctor validation before appointment creation to prevent booking with an invalid doctor.
        AppointmentRequestDTO request = buildAppointmentRequest();
        List<MedicalService> services = List.of(
                buildMedicalService(request.getMedicalServiceIds().get(0), 20),
                buildMedicalService(request.getMedicalServiceIds().get(1), 10)
        );
        Appointment appointment = buildAppointment();

        when(medicalServiceRepository.findAllById(request.getMedicalServiceIds())).thenReturn(services);
        when(slotService.validateAndUnlockSlot(any(), any(), any(), any())).thenReturn(true);
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any())).thenReturn(List.of());
        when(mapper.toAppointmentEntity(request, services)).thenReturn(appointment);

        appointmentService.createAppointment(request);

        verify(userServiceClient).getUserById(request.getDoctorId());
    }

    @Test
    @DisplayName("APP-SRV-UT-017 - Create appointment should validate patient before saving")
    void createAppointmentShouldValidatePatientBeforeSaving() {
        // Note: APP-SRV-UT-017 | Objective: require patient validation before appointment creation to prevent booking with an invalid patient.
        AppointmentRequestDTO request = buildAppointmentRequest();
        List<MedicalService> services = List.of(
                buildMedicalService(request.getMedicalServiceIds().get(0), 20),
                buildMedicalService(request.getMedicalServiceIds().get(1), 10)
        );
        Appointment appointment = buildAppointment();

        when(medicalServiceRepository.findAllById(request.getMedicalServiceIds())).thenReturn(services);
        when(slotService.validateAndUnlockSlot(any(), any(), any(), any())).thenReturn(true);
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any())).thenReturn(List.of());
        when(mapper.toAppointmentEntity(request, services)).thenReturn(appointment);

        appointmentService.createAppointment(request);

        verify(userServiceClient).getUserById(request.getPatientId());
    }

    @Test
    @DisplayName("APP-SRV-UT-018 - Update appointment successfully")
    void updateAppointmentShouldSaveUpdatedEntity() {
        // Note: APP-SRV-UT-017 | Objective: update an existing appointment and return the mapped DTO result.
        UUID appointmentId = UUID.randomUUID();
        AppointmentRequestDTO request = buildAppointmentRequest();
        request.setMedicalServiceIds(List.of(request.getMedicalServiceIds().get(0)));
        Appointment existing = buildAppointment();
        AppointmentDTO dto = buildAppointmentDto(existing);
        List<MedicalService> services = List.of(buildMedicalService(request.getMedicalServiceIds().get(0), 20));

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(existing));
        when(medicalServiceRepository.findAllById(request.getMedicalServiceIds())).thenReturn(services);
        when(mapper.toAppointmentDTO(existing)).thenReturn(dto);

        AppointmentDTO actual = appointmentService.updateAppointment(appointmentId, request);

        assertThat(actual).isSameAs(dto);
        verify(mapper).updateAppointmentEntity(existing, request, services);
        verify(appointmentRepository).save(existing);
    }

    @Test
    @DisplayName("APP-SRV-UT-019 - Update should preserve services when ids are omitted")
    void updateAppointmentShouldPreserveExistingServicesWhenMedicalServiceIdsAreNull() {
        // Note: APP-SRV-UT-019 | Objective: preserve existing medical services when update request does not include service ids.
        UUID appointmentId = UUID.randomUUID();
        Appointment existing = buildAppointment();
        AppointmentRequestDTO request = buildAppointmentRequest();
        request.setMedicalServiceIds(null);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(existing));
        when(mapper.toAppointmentDTO(existing)).thenReturn(buildAppointmentDto(existing));

        appointmentService.updateAppointment(appointmentId, request);

        verify(mapper).updateAppointmentEntity(existing, request, null);
    }

    @Test
    @DisplayName("APP-SRV-UT-020 - Reject update when appointment is missing")
    void updateAppointmentShouldThrowWhenAppointmentNotFound() {
        // Note: APP-SRV-UT-018 | Objective: throw exception when trying to update an appointment that does not exist.
        UUID appointmentId = UUID.randomUUID();
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.updateAppointment(appointmentId, buildAppointmentRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Appointment not found");
    }

    @Test
    @DisplayName("APP-SRV-UT-021 - Reject update when some medical service ids are invalid")
    void updateAppointmentShouldRejectInvalidMedicalServiceIds() {
        // Note: APP-SRV-UT-019 | Objective: reject appointment update when at least one requested medical service id is invalid.
        UUID appointmentId = UUID.randomUUID();
        Appointment existing = buildAppointment();
        AppointmentRequestDTO request = buildAppointmentRequest();

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(existing));
        when(medicalServiceRepository.findAllById(request.getMedicalServiceIds())).thenReturn(List.of(buildMedicalService(request.getMedicalServiceIds().get(0), 15)));

        assertThatThrownBy(() -> appointmentService.updateAppointment(appointmentId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Some medical services not found");

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("APP-SRV-UT-022 - Update appointment status successfully")
    void updateAppointmentStatusShouldPersistStatusChange() {
        // Note: APP-SRV-UT-020 | Objective: update appointment status and return mapped DTO after persistence.
        UUID appointmentId = UUID.randomUUID();
        Appointment existing = buildAppointment();
        AppointmentDTO dto = buildAppointmentDto(existing);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(existing));
        when(mapper.toAppointmentDTO(existing)).thenReturn(dto);

        AppointmentDTO actual = appointmentService.updateAppointmentStatus(appointmentId, AppointmentStatus.COMPLETED);

        assertThat(existing.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        assertThat(actual).isSameAs(dto);
        verify(appointmentRepository).save(existing);
    }

    @Test
    @DisplayName("APP-SRV-UT-023 - Start appointment successfully after check-in")
    void startAppointmentShouldMoveAppointmentToInProgress() {
        // Note: APP-SRV-UT-021 | Objective: move a checked-in appointment to IN_PROGRESS and return mapped DTO.
        UUID appointmentId = UUID.randomUUID();
        Appointment existing = buildAppointment();
        existing.setStatus(AppointmentStatus.CHECKED);
        AppointmentDTO dto = buildAppointmentDto(existing);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(existing));
        when(mapper.toAppointmentDTO(existing)).thenReturn(dto);

        AppointmentDTO actual = appointmentService.startAppointment(appointmentId);

        assertThat(existing.getStatus()).isEqualTo(AppointmentStatus.IN_PROGRESS);
        assertThat(actual).isSameAs(dto);
    }

    @Test
    @DisplayName("APP-SRV-UT-024 - Reject start when appointment is missing")
    void startAppointmentShouldThrowWhenAppointmentMissing() {
        // Note: APP-SRV-UT-022 | Objective: throw AppException when starting an appointment that does not exist.
        UUID appointmentId = UUID.randomUUID();
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.startAppointment(appointmentId))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(APPOINTMENT_NOT_EXISTED);
    }

    @Test
    @DisplayName("APP-SRV-UT-025 - Reject start when appointment has not checked in")
    void startAppointmentShouldThrowWhenAppointmentNotCheckedIn() {
        // Note: APP-SRV-UT-023 | Objective: reject start operation when the appointment status is not CHECKED.
        UUID appointmentId = UUID.randomUUID();
        Appointment existing = buildAppointment();
        existing.setStatus(AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> appointmentService.startAppointment(appointmentId))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(APPINTMENT_IS_NOT_CHECKIN_YET);
    }

    @Test
    @DisplayName("APP-SRV-UT-026 - Delete appointment by cancelling it")
    void deleteAppointmentShouldSetCancelledStatus() {
        // Note: APP-SRV-UT-024 | Objective: soft-delete an appointment by setting status to CANCELLED and saving it.
        UUID appointmentId = UUID.randomUUID();
        Appointment existing = buildAppointment();
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(existing));

        appointmentService.deleteAppointment(appointmentId);

        assertThat(existing.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        verify(appointmentRepository).save(existing);
    }

    @Test
    @DisplayName("APP-SRV-UT-027 - Reject delete when appointment is missing")
    void deleteAppointmentShouldThrowWhenMissing() {
        // Note: APP-SRV-UT-025 | Objective: throw exception when trying to delete an appointment that does not exist.
        UUID appointmentId = UUID.randomUUID();
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.deleteAppointment(appointmentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Appointment not found");
    }

    @Test
    @DisplayName("APP-SRV-UT-028 - Validate doctor successfully")
    void validateDoctorShouldSucceedForDoctorRole() {
        // Note: APP-SRV-UT-026 | Objective: accept a user as doctor when the user service returns the DOCTOR role.
        UUID doctorId = UUID.randomUUID();
        UserDTO doctor = new UserDTO();
        doctor.setRoles(Set.of("DOCTOR"));
        when(userServiceClient.getUserById(doctorId)).thenReturn(doctor);

        appointmentService.validateDoctor(doctorId);

        verify(userServiceClient).getUserById(doctorId);
    }

    @Test
    @DisplayName("APP-SRV-UT-029 - Reject doctor validation when roles are missing")
    void validateDoctorShouldThrowBusinessErrorWhenRolesAreMissing() {
        // Note: APP-SRV-UT-029 | Objective: return a business-level rejection when doctor roles are missing instead of throwing a NullPointerException.
        UUID doctorId = UUID.randomUUID();
        UserDTO doctor = new UserDTO();
        doctor.setRoles(null);
        when(userServiceClient.getUserById(doctorId)).thenReturn(doctor);

        assertThatThrownBy(() -> appointmentService.validateDoctor(doctorId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("bác");
    }

    @Test
    @DisplayName("APP-SRV-UT-030 - Reject patient validation when roles are missing")
    void validatePatientShouldThrowBusinessErrorWhenRolesAreMissing() {
        // Note: APP-SRV-UT-030 | Objective: return a business-level rejection when patient roles are missing instead of throwing a NullPointerException.
        UUID patientId = UUID.randomUUID();
        UserDTO patient = new UserDTO();
        patient.setRoles(null);
        when(userServiceClient.getUserById(patientId)).thenReturn(patient);

        assertThatThrownBy(() -> appointmentService.validatePatient(patientId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("bệnh");
    }

    @Test
    @DisplayName("APP-SRV-UT-031 - Reject doctor validation when user service throws")
    void validateDoctorShouldThrowEntityNotFoundWhenUserServiceFails() {
        // Note: APP-SRV-UT-027 | Objective: convert user-service lookup failure into EntityNotFoundException for doctor validation.
        UUID doctorId = UUID.randomUUID();
        when(userServiceClient.getUserById(doctorId)).thenThrow(new RuntimeException("downstream"));

        assertThatThrownBy(() -> appointmentService.validateDoctor(doctorId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(doctorId.toString());
    }

    private AppointmentRequestDTO buildAppointmentRequest() {
        ZonedDateTime start = fixedTime();
        return AppointmentRequestDTO.builder()
                .doctorId(UUID.randomUUID())
                .patientId(UUID.randomUUID())
                .appointmentStartTime(start)
                .appointmentEndTime(start.plusMinutes(30))
                .status(AppointmentStatus.CONFIRMED)
                .MedicalServiceIds(List.of(UUID.randomUUID(), UUID.randomUUID()))
                .build();
    }

    private Appointment buildAppointment() {
        return Appointment.builder()
                .id(UUID.randomUUID())
                .doctorId(UUID.randomUUID())
                .patientId(UUID.randomUUID())
                .appointmentStartTime(fixedTime())
                .appointmentEndTime(fixedTime().plusMinutes(30))
                .status(AppointmentStatus.CONFIRMED)
                .createdAt(fixedTime())
                .updatedAt(fixedTime())
                .medicalServices(List.of(buildMedicalService(UUID.randomUUID(), 20)))
                .build();
    }

    private AppointmentDTO buildAppointmentDto(Appointment appointment) {
        return AppointmentDTO.builder()
                .id(appointment.getId())
                .doctorId(appointment.getDoctorId())
                .patientId(appointment.getPatientId())
                .appointmentStartTime(appointment.getAppointmentStartTime())
                .appointmentEndTime(appointment.getAppointmentEndTime())
                .status(appointment.getStatus())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .medicalServices(List.of())
                .build();
    }

    private MedicalService buildMedicalService(UUID id, int serviceTime) {
        return MedicalService.builder()
                .id(id)
                .serviceName("X-Ray")
                .serviceType("IMAGING")
                .serviceTime(serviceTime)
                .status(MedicalServiceStatus.ACTIVE)
                .price(100f)
                .description("desc")
                .imgUrl("img")
                .build();
    }

    private ZonedDateTime fixedTime() {
        return ZonedDateTime.of(2026, 5, 13, 8, 0, 0, 0, ZoneOffset.UTC);
    }
}
