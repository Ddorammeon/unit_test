package com.main_project.appointment_service.service;

import com.main_project.appointment_service.dto.DoctorWorkScheduleDTO;
import com.main_project.appointment_service.dto.WorkScheduleDTO;
import com.main_project.appointment_service.entity.Appointment;
import com.main_project.appointment_service.entity.MedicalService;
import com.main_project.appointment_service.feignclient.DoctorServiceClient;
import com.main_project.appointment_service.repository.AppointmentRepository;
import com.main_project.appointment_service.repository.MedicalServiceRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SlotService implements ISlotService {

    private final AppointmentRepository appointmentRepository;
    private final MedicalServiceRepository medicalServiceRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final DoctorServiceClient doctorServiceClient;

    private static final String SLOT_KEY_PREFIX = "slot:";
    private static final int SLOT_STEP_MINUTE = 10;
    private static final long LOCK_EXPIRE_SEC = 15 * 60; // slot lock 15 phút để giữ slot trong 10 phút
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");

    @Override
    public List<ZonedDateTime> getAvailableSlots(UUID doctorId, UUID serviceId, ZonedDateTime date) {

        MedicalService service = medicalServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        int serviceTime = service.getServiceTime(); // phút

        WorkScheduleDTO workSchedule = resolveWorkSchedule(doctorId, date.toLocalDate());
        if (workSchedule == null) {
            return List.of();
        }

        ZonedDateTime workStart = workSchedule.getStartTime();
        ZonedDateTime workEnd   = workSchedule.getEndTime();

        // Lấy các appointment đã đặt trong ngày cho bác sĩ này
        List<Appointment> appointments = appointmentRepository
                .findByDoctorIdAndStartTimeBetween(doctorId, workStart, workEnd);

        List<ZonedDateTime> slots = new ArrayList<>();
        ZonedDateTime slotStart = workStart;

        while (!slotStart.plusMinutes(serviceTime).isAfter(workEnd)) {

            ZonedDateTime slotEnd = slotStart.plusMinutes(serviceTime);
            ZonedDateTime finalSlotStart = slotStart;

            // Kiểm tra trùng lịch với DB (khoảng [slotStart, slotEnd) vs [apptStart, apptEnd))
            boolean dbConflict = appointments.stream().anyMatch(a ->
                    !(slotEnd.isEqual(a.getAppointmentStartTime())
                            || slotEnd.isBefore(a.getAppointmentStartTime())
                            || a.getAppointmentEndTime().isEqual(finalSlotStart)
                            || a.getAppointmentEndTime().isBefore(finalSlotStart))
            );

            boolean redisConflict = hasRedisOverlap(doctorId, slotStart, slotEnd);

            // Slot chỉ được xem là "available" nếu không trùng DB và không bị giữ trong Redis
            if (!dbConflict && !redisConflict) {
                slots.add(slotStart);
            }

            // Bước nhảy 10 phút
            slotStart = slotStart.plusMinutes(SLOT_STEP_MINUTE);
        }
        return slots;
    }
    /**
     * USER click chọn slot → LOCK slot (theo doctor + startTime + patient)
     * Trả về true nếu giữ thành công, false nếu slot đã bị người khác giữ.
     */
    @Override
    public boolean lockSlot(UUID doctorId, UUID patientId, ZonedDateTime slotStart, ZonedDateTime slotEnd) {
        String key = buildKey(doctorId, slotStart);

        // Dùng setIfAbsent (SETNX) + TTL để đảm bảo atomic lock
        Boolean success = redisTemplate.opsForValue().setIfAbsent(
                key,
                serializeLock(patientId, slotStart, slotEnd),
                LOCK_EXPIRE_SEC,
                TimeUnit.SECONDS
        );

        return Boolean.TRUE.equals(success);
    }

    /**
     * USER bỏ chọn slot → UNLOCK slot (nhả luôn, không đợi 10 phút)
     */
    @Override
    public void unlockSlot(UUID doctorId, ZonedDateTime slotStart) {
        redisTemplate.delete(buildKey(doctorId, slotStart));
    }

    /**
     * Khi bệnh nhân ấn "Hoàn thành", trước khi tạo Appointment:
     * - Kiểm tra slot đang được giữ bởi đúng patientId không
     * - Nếu đúng → xoá key redis (nhả lock), trả true
     * - Nếu sai / hết hạn → trả false → báo lỗi "Slot hết hạn hoặc không hợp lệ"
     */
    @Override
    public boolean validateAndUnlockSlot(UUID doctorId, UUID patientId, ZonedDateTime slotStart, ZonedDateTime slotEnd) {
        String key = buildKey(doctorId, slotStart);
        Object value = redisTemplate.opsForValue().get(key);

        SlotLock lock = deserializeLock(value, slotStart);
        if (lock == null) {
            // TTL hết hoặc chưa từng lock
            return false;
        }

        if (!patientId.equals(lock.getPatientId())) {
            // Người khác đang giữ slot này
            return false;
        }

        if (lock.getEnd() != null && !lock.getEnd().isEqual(slotEnd)) {
            // Slot end-time khác với thời lượng đang giữ
            return false;
        }

        // Đúng bệnh nhân → xoá lock và cho phép tạo Appointment
        redisTemplate.delete(key);
        return true;
    }

    @Override
    public boolean isSlotAvailable(UUID doctorId, ZonedDateTime slotStart, ZonedDateTime slotEnd) {
        List<Appointment> overlapping = appointmentRepository
                .findOverlappingAppointments(doctorId, slotStart, slotEnd);

        if (!overlapping.isEmpty()) {
            return false;
        }

        return !hasRedisOverlap(doctorId, slotStart, slotEnd);
    }

    @Override
    public int calculateServiceDurationMinutes(List<UUID> medicalServiceIds) {
        if (CollectionUtils.isEmpty(medicalServiceIds)) {
            throw new RuntimeException("At least one medical service must be provided");
        }
        List<MedicalService> services = medicalServiceRepository.findAllById(medicalServiceIds);
        if (services.size() != medicalServiceIds.size()) {
            throw new RuntimeException("Some medical services not found");
        }
        return services.stream()
                .mapToInt(MedicalService::getServiceTime)
                .sum();
    }

    private String buildKey(UUID doctorId, ZonedDateTime slotStart) {
        String dateStr = slotStart.toLocalDate().format(DATE_FORMATTER);
        String timeStr = slotStart.toLocalTime().format(TIME_FORMATTER);
        return SLOT_KEY_PREFIX + doctorId + ":" + dateStr + ":" + timeStr;
    }

    private String buildDatePrefix(UUID doctorId, ZonedDateTime slotStart) {
        String dateStr = slotStart.toLocalDate().format(DATE_FORMATTER);
        return SLOT_KEY_PREFIX + doctorId + ":" + dateStr + ":";
    }

    private WorkScheduleDTO resolveWorkSchedule(UUID doctorId, LocalDate workDate) {
        try {
            List<DoctorWorkScheduleDTO> doctorSchedules = doctorServiceClient.getDoctorWorkSchedules(doctorId);
            if (CollectionUtils.isEmpty(doctorSchedules)) {
                return null;
            }

            for (DoctorWorkScheduleDTO doctorSchedule : doctorSchedules) {
                WorkScheduleDTO schedule = doctorServiceClient.getWorkSchedule(doctorSchedule.getWorkScheduleId());
                if (schedule != null && workDate.equals(schedule.getWorkDate())) {
                    return schedule;
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Cannot fetch doctor work schedule", ex);
        }
        return null;
    }

    private boolean hasRedisOverlap(UUID doctorId, ZonedDateTime start, ZonedDateTime end) {
        String prefix = buildDatePrefix(doctorId, start);
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys == null || keys.isEmpty()) {
            return false;
        }

        for (String key : keys) {
            ZonedDateTime lockedStartFromKey = parseSlotStartFromKey(key);
            SlotLock lock = deserializeLock(redisTemplate.opsForValue().get(key), lockedStartFromKey);
            if (lock == null) {
                // Nếu không parse được, coi như đang bị giữ để an toàn
                return true;
            }
            ZonedDateTime lockStart = lock.getStart() != null ? lock.getStart() : lockedStartFromKey;
            ZonedDateTime lockEnd = lock.getEnd() != null ? lock.getEnd() : lockStart.plusMinutes(SLOT_STEP_MINUTE);
            if (lockStart == null || lockEnd == null) {
                return true;
            }
            if (rangesOverlap(lockStart, lockEnd, start, end)) {
                return true;
            }
        }
        return false;
    }

    private boolean rangesOverlap(ZonedDateTime start1, ZonedDateTime end1, ZonedDateTime start2, ZonedDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private ZonedDateTime parseSlotStartFromKey(String key) {
        // key format: slot:{doctorId}:{yyyyMMdd}:{HHmm}
        try {
            String[] parts = key.split(":");
            if (parts.length < 4) {
                return null;
            }
            String dateStr = parts[2];
            String timeStr = parts[3];
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            LocalTime time = LocalTime.parse(timeStr, TIME_FORMATTER);
            return ZonedDateTime.of(date, time, ZoneId.systemDefault());
        } catch (Exception ex) {
            return null;
        }
    }

    private String serializeLock(UUID patientId, ZonedDateTime start, ZonedDateTime end) {
        return new SlotLock(patientId, start, end).toPayload();
    }

    private SlotLock deserializeLock(Object value, ZonedDateTime fallbackStart) {
        if (value == null) {
            return null;
        }
        if (value instanceof SlotLock lock) {
            return lock;
        }
        return SlotLock.fromPayload(value.toString(), fallbackStart);
    }

    @Data
    @AllArgsConstructor
    private static class SlotLock {
        private UUID patientId;
        private ZonedDateTime start;
        private ZonedDateTime end;

        String toPayload() {
            return patientId + "|" + start + "|" + end;
        }

        static SlotLock fromPayload(String payload, ZonedDateTime fallbackStart) {
            if (payload == null || payload.isEmpty()) {
                return null;
            }
            String[] parts = payload.split("\\|");
            try {
                UUID patient = UUID.fromString(parts[0]);
                ZonedDateTime start = parts.length > 1 && !parts[1].isEmpty() ? ZonedDateTime.parse(parts[1]) : fallbackStart;
                ZonedDateTime end = parts.length > 2 && !parts[2].isEmpty() ? ZonedDateTime.parse(parts[2]) : null;
                return new SlotLock(patient, start, end);
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
