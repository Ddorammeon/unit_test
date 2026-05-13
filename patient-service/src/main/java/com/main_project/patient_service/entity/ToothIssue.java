package com.main_project.patient_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * ToothIssue - Child Entity (Part of Patient Aggregate)
 *
 * Represents a specific dental issue linked to a tooth number.
 * This entity is owned by Patient and cannot exist independently.
 * IMPORTANT: NO repository for this entity - managed through Patient only.
 */
@Entity
@Table(name = "tooth_issue")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToothIssue {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "tooth_number", nullable = false)
    private Integer toothNumber; // Dental notation (1-32 for adults)

    @Column(name = "status", length = 50)
    private String status; // e.g., "ACTIVE", "TREATED", "PENDING"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // e.g., "Cavity", "Root canal needed"

    @Column(name = "diagnosed_date")
    private LocalDate diagnosedDate;

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
        if (diagnosedDate == null) {
            diagnosedDate = LocalDate.now();
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
