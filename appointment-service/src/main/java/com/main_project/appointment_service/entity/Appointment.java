package com.main_project.appointment_service.entity;

import com.main_project.appointment_service.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "appointment")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, name = "doctor_id")
    private UUID doctorId;

    @Column(nullable = false, name = "patient_id")
    private UUID patientId;

    @Column(nullable = false, name = "appointment_start_time")
    private ZonedDateTime appointmentStartTime;

    @Column(nullable = false, name = "appointment_end_time")
    private ZonedDateTime appointmentEndTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 255, name = "status")
    private AppointmentStatus status;

    @Column(nullable = false, name = "created_at")
    private ZonedDateTime createdAt;

    @Column(nullable = false, name = "updated_at")
    private ZonedDateTime updatedAt;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "appointment_medical_service",
            joinColumns = @JoinColumn(name = "appointment_id"),
            inverseJoinColumns = @JoinColumn(name = "medical_service_id")
    )
    @Builder.Default
    private List<MedicalService> medicalServices = new ArrayList<>();;
}
