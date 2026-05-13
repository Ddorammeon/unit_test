package com.main_project.doctor_service.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EntityLifecycleTest {

    @Test
    @DisplayName("LIFE-UT-001 - DoctorDegree onCreate should generate id")
    void doctorDegreeOnCreateShouldGenerateIdWhenMissing() {
        // Note: LIFE-UT-001 | Objective: ensure DoctorDegree generates a UUID during onCreate when id is not set.
        DoctorDegree degree = DoctorDegree.builder().id(null).build();

        degree.onCreate();

        assertThat(degree.getId()).isNotNull();
    }

    @Test
    @DisplayName("LIFE-UT-002 - WorkSchedule onCreate should generate id")
    void workScheduleOnCreateShouldGenerateIdWhenMissing() {
        // Note: LIFE-UT-002 | Objective: ensure WorkSchedule generates a UUID during onCreate when id is not set.
        WorkSchedule schedule = WorkSchedule.builder().id(null).build();

        schedule.onCreate();

        assertThat(schedule.getId()).isNotNull();
    }

    @Test
    @DisplayName("LIFE-UT-003 - DoctorWorkSchedule onCreate should set id and timestamps")
    void doctorWorkScheduleOnCreateShouldSetIdAndTimestamps() {
        // Note: LIFE-UT-003 | Objective: ensure DoctorWorkSchedule generates id and initializes createdAt and updatedAt in UTC.
        DoctorWorkSchedule entity = DoctorWorkSchedule.builder().id(null).build();

        entity.onCreate();

        assertThat(entity.getId()).isNotNull();
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isEqualTo(entity.getCreatedAt());
        assertThat(entity.getCreatedAt().getOffset()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    @DisplayName("LIFE-UT-004 - DoctorWorkSchedule onUpdate should refresh updatedAt")
    void doctorWorkScheduleOnUpdateShouldRefreshUpdatedAt() {
        // Note: LIFE-UT-004 | Objective: ensure DoctorWorkSchedule updates updatedAt to a newer timestamp during onUpdate.
        DoctorWorkSchedule entity = DoctorWorkSchedule.builder().build();
        entity.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(5));

        ZonedDateTime beforeUpdate = entity.getUpdatedAt();
        entity.onUpdate();

        assertThat(entity.getUpdatedAt()).isAfter(beforeUpdate);
    }
}
