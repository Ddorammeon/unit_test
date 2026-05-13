package com.do_an.invoiceservice.repository;

import com.do_an.invoiceservice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    // API lọc theo trạng thái
    List<Invoice> findAllByStatus(String status);

    // API lọc theo lịch hẹn
    List<Invoice> findAllByAppointmentId(UUID appointmentId);
    
    // API lọc theo receptionist ID
    List<Invoice> findAllByReceptionistId(UUID receptionistId);
    
    // API lọc theo receptionist ID và status
    List<Invoice> findAllByReceptionistIdAndStatus(UUID receptionistId, String status);
    
    // API lọc theo appointment ID và status
    List<Invoice> findAllByAppointmentIdAndStatus(UUID appointmentId, String status);
    
    // API lọc theo khoảng thời gian tạo

    List<Invoice> findAllByPaidAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // API lọc theo khoảng thời gian issue
    List<Invoice> findAllByIssueAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // API sắp xếp theo thời gian tạo giảm dần
    List<Invoice> findAllByOrderByIssueAtDesc();
    
    // API sắp xếp theo thời gian tạo tăng dần
    List<Invoice> findAllByOrderByIssueAtAsc();

    List<Invoice> findAllByStatusAndIssueAtBefore(String status, LocalDateTime issueAt);
    List<Invoice> findByStatusOrderByIssueAtDesc(String status);

    // Thêm method mới để query nhiều appointmentIds cùng lúc
    List<Invoice> findAllByAppointmentIdIn(List<UUID> appointmentIds);

    // Query với appointmentIds và status
    List<Invoice> findAllByAppointmentIdInAndStatus(List<UUID> appointmentIds, String status);

    // Query với appointmentIds và sắp xếp theo thời gian
    List<Invoice> findAllByAppointmentIdInOrderByIssueAtDesc(List<UUID> appointmentIds);

    // Query với appointmentIds, status và sắp xếp
    List<Invoice> findAllByAppointmentIdInAndStatusOrderByIssueAtDesc(List<UUID> appointmentIds, String status);


}
