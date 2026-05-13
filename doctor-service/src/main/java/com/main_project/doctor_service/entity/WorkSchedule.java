package com.main_project.doctor_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "work_schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkSchedule {

    @Id
    @Column(length = 50)
    private UUID id;

    @Column(name = "work_date")
    private LocalDate workDate;

    @Column(name = "start_time")
    private ZonedDateTime startTime;

    @Column(name = "end_time")
    private ZonedDateTime endTime;

    @PrePersist
    public void onCreate() {
        if (id == null || id.toString().isBlank()) {
            id = UUID.randomUUID();
        }
    }
}
