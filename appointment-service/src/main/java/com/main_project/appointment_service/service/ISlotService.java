package com.main_project.appointment_service.service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface ISlotService {
    List<ZonedDateTime> getAvailableSlots(UUID doctorId, UUID serviceId, ZonedDateTime date);

    boolean lockSlot(UUID doctorId, UUID patientId, ZonedDateTime slotStart, ZonedDateTime slotEnd);

    void unlockSlot(UUID doctorId, ZonedDateTime slotStart);
    boolean validateAndUnlockSlot(UUID doctorId, UUID patientId, ZonedDateTime slotStart, ZonedDateTime slotEnd);

    boolean isSlotAvailable(UUID doctorId, ZonedDateTime slotStart, ZonedDateTime slotEnd);

    int calculateServiceDurationMinutes(List<UUID> medicalServiceIds);
}
