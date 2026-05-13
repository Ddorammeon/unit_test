package com.main_project.doctor_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "doctor_degree")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorDegree {

    @Id
    @Column(length = 50)
    private UUID id;

    @Column(name = "degree_name", length = 255)
    private String degreeName;

    @Column(length = 255)
    private String institution;

    @Column(name = "year_obtained")
    private Integer yearObtained;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", referencedColumnName = "user_id")
    private Doctor doctor;

    @PrePersist
    public void onCreate() {
        if (id == null || id.toString().isBlank()) {
            id = UUID.randomUUID();
        }
    }
}
