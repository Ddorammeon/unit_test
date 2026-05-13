package com.main_project.insurance_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "claim_document")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "status")
    private String status;

    @Column(name = "upload_at")
    private LocalDateTime uploadAt;

    // Many-to-One relationship with InsuranceClaim
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_claim_id", referencedColumnName = "id")
    private InsuranceClaim insuranceClaim;
}





