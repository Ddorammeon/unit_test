package com.main_project.patient_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * PatientAllergy - Join Entity (Part of Patient Aggregate)
 *
 * This entity represents the many-to-many relationship between Patient and Allergy
 * with additional fields (severity, reaction, note).
 *
 * IMPORTANT: This is part of the Patient aggregate and should NOT have its own repository.
 * All operations must go through the Patient aggregate root.
 */
@Entity
@Table(name = "patient_allergy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientAllergy {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "severity", length = 50)
    private String severity; // MILD, MODERATE, SEVERE

    @Column(name = "reaction", length = 255)
    private String reaction; // e.g., "Rash", "Difficulty breathing"

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // Parent relationship (composition)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "user_id", nullable = false)
    private Patient patient;

    // Reference to master data
    @ManyToOne(fetch = FetchType.EAGER) // EAGER because we always need allergy details
    @JoinColumn(name = "allergy_id", referencedColumnName = "id", nullable = false)
    private Allergy allergy;

    @PrePersist
    public void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    /**
     * Helper method to set the patient relationship.
     * Package-private so only accessible within aggregate.
     */
    void setPatientInternal(Patient patient) {
        this.patient = patient;
    }
}
