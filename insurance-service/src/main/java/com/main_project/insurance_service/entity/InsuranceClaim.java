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
@Table(name = "insurance_claim")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceClaim {

    @Id
    private UUID id;

    @Column(name = "status")
    private String status;

    @Column(name = "patient_pay_amount")
    private Integer patientPayAmount;

    @Column(name = "total_claim_amount")
    private Integer totalClaimAmount;

    @Column(name = "total_insurance_pay")
    private Integer totalInsurancePay;

    @Column(name = "claim_date")
    private LocalDateTime claimDate;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "create_at")
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "update_at")
    private LocalDateTime updateAt;

    // Many-to-One relationship with PatientInsurance
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_insurance_id", referencedColumnName = "id")
    private PatientInsurance patientInsurance;

    // One-to-Many relationship with ClaimDocument
    @OneToMany(mappedBy = "insuranceClaim", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClaimDocument> claimDocuments;

    // One-to-Many relationship with ClaimItem
    @OneToMany(mappedBy = "insuranceClaim", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClaimItem> claimItems;
}





