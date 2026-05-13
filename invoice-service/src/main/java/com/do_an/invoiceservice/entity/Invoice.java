package com.do_an.invoiceservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "invoice")
@Getter
@Setter
public class Invoice {

    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id; // varchar(50)


    private UUID receptionistId;
    private UUID appointmentId;
    private Integer totalAmount;
    private String currency;
    private String status;

    private LocalDateTime issueAt;
    private LocalDateTime paidAt;

    private Integer insuranceTotalPay;
    private Integer patientTotalPay;
    private UUID insuranceClaimId;


    @UpdateTimestamp
    private LocalDateTime updateAt;

    // Quan hệ 1-Nhiều
    // CascadeType.ALL: Khi lưu/xóa Invoice, các Item con cũng được lưu/xóa
    // orphanRemoval = true: Khi xóa một Item khỏi Set này, nó sẽ bị xóa khỏi DB
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<InvoiceItem> items = new HashSet<>();

    // Hàm helper để đồng bộ hóa hai chiều
    public void addItem(InvoiceItem item) {
        items.add(item);
        item.setInvoice(this);
    }
}