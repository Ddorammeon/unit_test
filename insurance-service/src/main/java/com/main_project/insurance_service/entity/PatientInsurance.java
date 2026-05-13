package com.main_project.insurance_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "patient_insurance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientInsurance {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "patient_id", unique = true, nullable = false)
    private UUID patientId;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "status")
    private String status;

    @CreationTimestamp
    @Column(name = "create_at")
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "update_at")
    private LocalDateTime updateAt;

    // Many-to-One relationship with InsurancePolicy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_policy_id", referencedColumnName = "id")
    private InsurancePolicy insurancePolicy;

    // One-to-Many relationship with InsuranceClaim
    @OneToMany(mappedBy = "patientInsurance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InsuranceClaim> insuranceClaims;
}



