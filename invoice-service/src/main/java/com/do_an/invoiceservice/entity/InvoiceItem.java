package com.do_an.invoiceservice.entity;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoice_item")
@Getter
@Setter
public class InvoiceItem {
    @Id
    private UUID id;

    private UUID referenceId;
    private String serviceType;
    private Integer quantity;
    private String description;
    private Integer unitPrice;

    private Integer insurancePayAmount;
    private Integer patientPayAmount;

    private UUID claimItemId;

    // Quan hệ Nhiều-1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)

    private Invoice invoice;
}
