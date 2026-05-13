package com.main_project.appointment_service.service;

import com.main_project.appointment_service.dto.DoctorWorkScheduleDTO;
import com.main_project.appointment_service.dto.WorkScheduleDTO;
import com.main_project.appointment_service.entity.Appointment;
import com.main_project.appointment_service.entity.MedicalService;
import com.main_project.appointment_service.feignclient.DoctorServiceClient;
import com.main_project.appointment_service.repository.AppointmentRepository;
import com.main_project.appointment_service.repository.MedicalServiceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private MedicalServiceRepository medicalServiceRepository;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private DoctorServiceClient doctorServiceClient;
    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private SlotService slotService;

    @Test
    @DisplayName("SLOT-SRV-UT-001 - Return empty slots when no work schedule exists")
    void getAvailableSlotsShouldReturnEmptyWhenNoWorkScheduleExists() {
        // Note: SLOT-SRV-UT-001 | Objective: return no slots when doctor-service has no matching work schedule for the date.
        UUID doctorId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        ZonedDateTime date = fixedTime();
        when(medicalServiceRepository.findById(serviceId)).thenReturn(java.util.Optional.of(buildMedicalService(serviceId, 20)));
        when(doctorServiceClient.getDoctorWorkSchedules(doctorId)).thenReturn(List.of());

        assertThat(slotService.getAvailableSlots(doctorId, serviceId, date)).isEmpty();
    }

    @Test
    @DisplayName("SLOT-SRV-UT-002 - Return available slots based on work schedule")
    void getAvailableSlotsShouldReturnCalculatedSlots() {
        // Note: SLOT-SRV-UT-002 | Objective: calculate available slots in 10-minute steps within the doctor's work schedule window.
        UUID doctorId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UUID workScheduleId = UUID.randomUUID();
        ZonedDateTime date = fixedTime();
        WorkScheduleDTO schedule = new WorkScheduleDTO();
        schedule.setId(workScheduleId);
        schedule.setWorkDate(date.toLocalDate());
        schedule.setStartTime(date.withHour(9).withMinute(0));
        schedule.setEndTime(date.withHour(10).withMinute(0));
        DoctorWorkScheduleDTO doctorSchedule = new DoctorWorkScheduleDTO();
        doctorSchedule.setWorkScheduleId(workScheduleId);

        when(medicalServiceRepository.findById(serviceId)).thenReturn(java.util.Optional.of(buildMedicalService(serviceId, 20)));
        when(doctorServiceClient.getDoctorWorkSchedules(doctorId)).thenReturn(List.of(doctorSchedule));
        when(doctorServiceClient.getWorkSchedule(workScheduleId)).thenReturn(schedule);
        when(appointmentRepository.findByDoctorIdAndStartTimeBetween(doctorId, schedule.getStartTime(), schedule.getEndTime())).thenReturn(List.of());
        when(redisTemplate.keys(any())).thenReturn(Set.of());

        List<ZonedDateTime> actual = slotService.getAvailableSlots(doctorId, serviceId, date);

        assertThat(actual).hasSize(5);
        assertThat(actual.get(0)).isEqualTo(schedule.getStartTime());
        assertThat(actual.get(4)).isEqualTo(schedule.getStartTime().plusMinutes(40));
    }

    @Test
    @DisplayName("SLOT-SRV-UT-003 - Lock slot successfully")
    void lockSlotShouldReturnTrueWhenRedisLockSucceeds() {
        // Note: SLOT-SRV-UT-003 | Objective: return true when Redis setIfAbsent successfully locks a slot.
        UUID doctorId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        ZonedDateTime start = fixedTime();
        ZonedDateTime end = start.plusMinutes(20);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), eq(900L), eq(TimeUnit.SECONDS))).thenReturn(true);

        assertThat(slotService.lockSlot(doctorId, patientId, start, end)).isTrue();
    }

    @Test
    @DisplayName("SLOT-SRV-UT-004 - Reject lock slot when Redis lock fails")
    void lockSlotShouldReturnFalseWhenRedisLockFails() {
        // Note: SLOT-SRV-UT-004 | Objective: return false when Redis setIfAbsent cannot lock the requested slot.
        UUID doctorId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        ZonedDateTime start = fixedTime();
        ZonedDateTime end = start.plusMinutes(20);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), eq(900L), eq(TimeUnit.SECONDS))).thenReturn(false);

        assertThat(slotService.lockSlot(doctorId, patientId, start, end)).isFalse();
    }

    @Test
    @DisplayName("SLOT-SRV-UT-005 - Unlock slot should delete Redis key")
    void unlockSlotShouldDeleteKey() {
        // Note: SLOT-SRV-UT-005 | Objective: delete the Redis key representing a locked slot during unlock.
        UUID doctorId = UUID.randomUUID();
        ZonedDateTime start = fixedTime();

        slotService.unlockSlot(doctorId, start);

        verify(redisTemplate).delete(anyString());
    }

    @Test
    @DisplayName("SLOT-SRV-UT-006 - Validate and unlock slot successfully")
    void validateAndUnlockSlotShouldDeleteKeyAndReturnTrue() {
        // Note: SLOT-SRV-UT-006 | Objective: return true and delete the Redis key when the slot belongs to the same patient and end time.
        UUID doctorId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        ZonedDateTime start = fixedTime();
        ZonedDateTime end = start.plusMinutes(20);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenReturn(patientId + "|" + start + "|" + end);

        assertThat(slotService.validateAndUnlockSlot(doctorId, patientId, start, end)).isTrue();
        verify(redisTemplate).delete(anyString());
    }

    @Test
    @DisplayName("SLOT-SRV-UT-007 - Reject validate and unlock when slot lock is missing")
    void validateAndUnlockSlotShouldReturnFalseWhenLockMissing() {
        // Note: SLOT-SRV-UT-007 | Objective: return false when no Redis lock exists for the requested slot.
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenReturn(null);

        assertThat(slotService.validateAndUnlockSlot(UUID.randomUUID(), UUID.randomUUID(), fixedTime(), fixedTime().plusMinutes(20))).isFalse();
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    @DisplayName("SLOT-SRV-UT-008 - Reject validate and unlock when patient id does not match")
    void validateAndUnlockSlotShouldReturnFalseWhenPatientDoesNotMatch() {
        // Note: SLOT-SRV-UT-008 | Objective: return false when the Redis lock is held by a different patient id.
        UUID expectedPatient = UUID.randomUUID();
        ZonedDateTime start = fixedTime();
        ZonedDateTime end = start.plusMinutes(20);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenReturn(UUID.randomUUID() + "|" + start + "|" + end);

        assertThat(slotService.validateAndUnlockSlot(UUID.randomUUID(), expectedPatient, start, end)).isFalse();
    }

    @Test
    @DisplayName("SLOT-SRV-UT-009 - Reject validate and unlock when end time does not match")
    void validateAndUnlockSlotShouldReturnFalseWhenEndTimeDoesNotMatch() {
        // Note: SLOT-SRV-UT-009 | Objective: return false when the Redis lock end time differs from the requested slot end time.
        UUID patientId = UUID.randomUUID();
        ZonedDateTime start = fixedTime();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenReturn(patientId + "|" + start + "|" + start.plusMinutes(30));

        assertThat(slotService.validateAndUnlockSlot(UUID.randomUUID(), patientId, start, start.plusMinutes(20))).isFalse();
    }

    @Test
    @DisplayName("SLOT-SRV-UT-013 - Reject duration calculation when service ids are empty")
    void calculateServiceDurationMinutesShouldThrowWhenListEmpty() {
        // Note: SLOT-SRV-UT-013 | Objective: throw exception when calculating duration without any medical service ids.
        assertThatThrownBy(() -> slotService.calculateServiceDurationMinutes(List.of()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("At least one medical service");
    }

    @Test
    @DisplayName("SLOT-SRV-UT-014 - Reject duration calculation when some services are missing")
    void calculateServiceDurationMinutesShouldThrowWhenServicesMissing() {
        // Note: SLOT-SRV-UT-014 | Objective: throw exception when at least one requested medical service id cannot be resolved.
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(medicalServiceRepository.findAllById(ids)).thenReturn(List.of(buildMedicalService(ids.get(0), 20)));

        assertThatThrownBy(() -> slotService.calculateServiceDurationMinutes(ids))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Some medical services not found");
    }

    @Test
    @DisplayName("SLOT-SRV-UT-015 - Calculate total service duration successfully")
    void calculateServiceDurationMinutesShouldReturnSum() {
        // Note: SLOT-SRV-UT-015 | Objective: sum all service durations and return the total number of minutes.
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(medicalServiceRepository.findAllById(ids)).thenReturn(List.of(buildMedicalService(ids.get(0), 20), buildMedicalService(ids.get(1), 10)));

        assertThat(slotService.calculateServiceDurationMinutes(ids)).isEqualTo(30);
    }

    private MedicalService buildMedicalService(UUID id, int minutes) {
        return MedicalService.builder()
                .id(id)
                .serviceName("X-Ray")
                .serviceType("IMAGING")
                .serviceTime(minutes)
                .price(100f)
                .build();
    }

    private ZonedDateTime fixedTime() {
        return ZonedDateTime.of(2026, 5, 13, 8, 0, 0, 0, ZoneOffset.UTC);
    }
}
