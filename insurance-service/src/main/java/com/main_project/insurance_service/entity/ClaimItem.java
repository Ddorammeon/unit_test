package com.main_project.insurance_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "claim_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimItem {

    @Id
    //@GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "unit_price")
    private Integer unitPrice;

    @Column(name = "total_amount")
    private Integer totalAmount;

    @Column(name = "insurance_pay_ratio")
    private Float insurancePayRatio;

    @Column(name = "insurance_pay_amount")
    private Integer insurancePayAmount;

    @Column(name = "patient_pay_amount")
    private Integer patientPayAmount;

    // Many-to-One relationship with InsuranceClaim
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_claim_id", referencedColumnName = "id")
    private InsuranceClaim insuranceClaim;

    // Many-to-One relationship with BhytCatalogue
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bhyt_catalogue_id", referencedColumnName = "id")
    private BhytCatalogue bhytCatalogue;
}

