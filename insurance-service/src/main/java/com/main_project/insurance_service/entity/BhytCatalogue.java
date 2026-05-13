package com.main_project.insurance_service.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bhyt_catalogue")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BhytCatalogue {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "service_code", length = 50)
    private String serviceCode;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "service_type", length = 50)
    private String serviceType;

    @Column(name = "is_covered")
    private Boolean isCovered;

    @Column(name = "max_coverage_amount")
    private Integer maxCoverageAmount;

    // One-to-Many relationship with ClaimItem
    @OneToMany(mappedBy = "bhytCatalogue", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClaimItem> claimItems;
}

