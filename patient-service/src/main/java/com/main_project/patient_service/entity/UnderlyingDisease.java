package com.main_project.patient_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * UnderlyingDisease - Child Entity (Part of Patient Aggregate)
 *
 * Represents a specific medical condition of the patient.
 * This entity is owned by Patient and cannot exist independently.
 * IMPORTANT: NO repository for this entity - managed through Patient only.
 */
@Entity
@Table(name = "underlying_disease")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnderlyingDisease {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", length = 255, nullable = false)
    private String name; // e.g., "Diabetes Type 2", "Hypertension"

    @Column(name = "status", length = 50)
    private String status; // e.g., "ACTIVE", "CONTROLLED", "RESOLVED"

    @Column(name = "severity", length = 50)
    private String severity; // e.g., "MILD", "MODERATE", "SEVERE"

    @Column(name = "is_verified")
    private Boolean isVerified; // Whether clinically verified

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "user_id", nullable = false)
    private Patient patient;

    @PrePersist
    public void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (isVerified == null) {
            isVerified = false;
        }
    }

    /**
     * Package-private method for setting patient relationship.
     * Only accessible within the aggregate.
     */
    void setPatientInternal(Patient patient) {
        this.patient = patient;
    }
}
