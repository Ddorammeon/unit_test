package com.main_project.appointment_service.entity;

import com.main_project.appointment_service.enums.MedicalServiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "medical_service")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalService {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String serviceName;

    @Column(nullable = false, length = 255)
    private String serviceType;

    @Column(nullable = false)
    private Integer serviceTime;  // don vi: phut

    @Column(nullable = false)
    private Float price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String imgUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 255)
    private MedicalServiceStatus status;

    // Many-to-Many voi Appointment
    @ManyToMany(mappedBy = "medicalServices")
    private List<Appointment> appointments;
}
