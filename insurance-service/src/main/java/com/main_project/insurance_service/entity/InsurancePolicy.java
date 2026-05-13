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
@Table(name = "insurance_policy")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsurancePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "policy_number", unique = true, nullable = false)
    private String policyNumber;

    @Column(name = "policy_type")
    private String policyType;

    @Column(name = "coverage_amount")
    private Integer coverageAmount;

    @Column(name = "deductible")
    private Integer deductible;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status")
    private String status;

    @CreationTimestamp
    @Column(name = "create_at")
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "update_at")
    private LocalDateTime updateAt;

    // One-to-Many relationship with PatientInsurance
    @OneToMany(mappedBy = "insurancePolicy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PatientInsurance> patientInsurances;
}


