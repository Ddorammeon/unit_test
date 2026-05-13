package com.main_project.patient_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Allergy - Master Data Entity
 *
 * This is an independent catalog of allergens.
 * It exists separately from Patient and is referenced by PatientAllergy.
 */
@Entity
@Table(name = "allergy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Allergy {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "type", length = 100)
    private String type; // e.g., "FOOD", "DRUG", "ENVIRONMENTAL"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @PrePersist
    public void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
