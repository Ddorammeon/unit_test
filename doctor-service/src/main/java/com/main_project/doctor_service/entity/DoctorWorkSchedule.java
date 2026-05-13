package com.main_project.doctor_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "doctor_work_schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorWorkSchedule {

    @Id
    @Column(length = 50)
    private UUID id;

    @Column(length = 255)
    private String status;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", referencedColumnName = "user_id")
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_schedule_id", referencedColumnName = "id")
    private WorkSchedule workSchedule;

    @PrePersist
    public void onCreate() {
        if (id == null || id.toString().isBlank()) {
            id = UUID.randomUUID();
        }
        createdAt = ZonedDateTime.now(ZoneOffset.UTC);
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = ZonedDateTime.now(ZoneOffset.UTC);
    }
}
