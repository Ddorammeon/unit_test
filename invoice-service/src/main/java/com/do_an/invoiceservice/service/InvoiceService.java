package com.do_an.invoiceservice.service;

import com.do_an.common.model.InvoiceItemCheckerRequest;
import com.do_an.common.model.InvoiceItemResponse;
import com.do_an.common.model.MedicineItem;
import com.do_an.invoiceservice.client.AppointmentClient;
import com.do_an.invoiceservice.dto.request.CreateInvoiceItemRequestDTO;
import com.do_an.invoiceservice.dto.request.CreateInvoiceRequestDTO;
import com.do_an.invoiceservice.dto.response.AppointmentDTO;
import com.do_an.invoiceservice.dto.response.InvoiceResponseDTO;
import com.do_an.invoiceservice.entity.Invoice;
import com.do_an.invoiceservice.entity.InvoiceItem;
import com.do_an.invoiceservice.exception.InvoiceNotFoundException;
import com.do_an.invoiceservice.iservice.IInvoiceService;
import com.do_an.invoiceservice.mapper.InvoiceItemMapper;
import com.do_an.invoiceservice.mapper.InvoiceMapper;
import com.do_an.invoiceservice.repository.InvoiceItemRepository;
import com.do_an.invoiceservice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService implements IInvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceItemMapper invoiceItemMapper;
    private final AppointmentClient appointmentClient;


    /**
     * CHỨC NĂNG 1: Tạo Hóa đơn mới (Transaction)
     */
    @Transactional
    public InvoiceResponseDTO createInvoice(CreateInvoiceRequestDTO request) {

        Invoice invoice = invoiceMapper.toEntity(request);
        //String invoiceId = "invoice-" + (System.currentTimeMillis() % 10000000000L); // chỉ lấy 10 chữ số cuối
        //invoice.setId(UUID.fromString(invoiceId));
        invoice.setId(request.getId());
        invoice.setStatus("PENDING"); // <-- THAY ĐỔI: Bắt đầu là PENDING
        invoice.setIssueAt(LocalDateTime.now());

        int totalAmount = 0;
        for (InvoiceItem item : invoice.getItems()) {
            //item.setId(UUID.randomUUID());
            item.setPatientPayAmount(item.getQuantity() * item.getUnitPrice());
            item.setInsurancePayAmount(0);
            item.setInvoice(invoice);
            totalAmount += (item.getQuantity() * item.getUnitPrice());
        }
        invoice.setTotalAmount(totalAmount);
        invoice.setPatientTotalPay(totalAmount);
        invoice.setInsuranceTotalPay(0);
        Invoice savedInvoice = invoiceRepository.save(invoice);
        savedInvoice.getItems().size(); // ép load các item
        return invoiceMapper.toResponseDto(savedInvoice);
    }

    /**
     * HÀM MỚI: Cập nhật Hóa đơn (chỉ khi là PENDING)
     */
    @Transactional
    public InvoiceResponseDTO updateInvoice(UUID invoiceId, CreateInvoiceRequestDTO request) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Không tìm thấy hóa đơn"));

        // Chỉ cho phép sửa khi là DRAFT
        if (!"PENDING".equals(invoice.getStatus())) {
            throw new IllegalStateException("Chỉ các hóa đơn PENDING mới có thể được cập nhật.");
        }

        // 1. Cập nhật header
        invoice.setReceptionistId(request.getReceptionistId());
        invoice.setAppointmentId(request.getAppointmentId());
        invoice.setCurrency(request.getCurrency());

        // 2. Đồng bộ hóa Items (logic phức tạp)
        syncInvoiceItems(invoice, request.getItems());

        // 3. Tính toán lại tổng tiền
        int newTotalAmount = invoice.getItems().stream()
                .mapToInt(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
        invoice.setTotalAmount(newTotalAmount);

        Invoice savedInvoice = invoiceRepository.save(invoice);
        return invoiceMapper.toResponseDto(savedInvoice);
    }

    /**
     * HÀM HELPER: Đồng bộ (Thêm/Sửa/Xóa) các items
     */
    private void syncInvoiceItems(Invoice invoice, List<CreateInvoiceItemRequestDTO> dtos) {
        // 1. Lấy Map các item DTO có ID (để cập nhật)
        Map<UUID, CreateInvoiceItemRequestDTO> dtoMap = dtos.stream()
                .filter(dto -> dto.getId() != null)
                .collect(Collectors.toMap(CreateInvoiceItemRequestDTO::getId, Function.identity()));

        // 2. Xử lý XÓA
        List<InvoiceItem> existingItems = invoiceItemRepository.findByInvoiceId(invoice.getId());
        List<InvoiceItem> itemsToRemove = existingItems.stream()
                .filter(existing -> !dtoMap.containsKey(existing.getId()))
                .collect(Collectors.toList());

        invoice.getItems().removeAll(itemsToRemove); // Xóa khỏi collection
        invoiceItemRepository.deleteAll(itemsToRemove); // Xóa khỏi DB

        // 3. Xử lý THÊM / SỬA
        for (CreateInvoiceItemRequestDTO dto : dtos) {
            if (dto.getId() == null) {
                // THÊM MỚI
                InvoiceItem newItem = invoiceItemMapper.toEntity(dto); // Dùng mapper
                //newItem.setId(UUID.randomUUID());
                invoice.addItem(newItem); // Thêm vào collection (để Cascade lưu)
            } else {
                // CẬP NHẬT
                InvoiceItem existingItem = existingItems.stream()
                        .filter(item -> item.getId().equals(dto.getId()))
                        .findFirst()
                        .orElse(null);

                if (existingItem != null) {
                    // Dùng mapper để cập nhật (an toàn)
                    invoiceItemMapper.updateFromDto(dto, existingItem);
                }
            }
        }
    }



    /**
     * CHỨC NĂNG 2: Đánh dấu Đã thanh toán
     */
    @Transactional
    public InvoiceResponseDTO markAsPaid(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Không tìm thấy hoá đơn"));

        if (!"PENDING".equals(invoice.getStatus())) {
            throw new IllegalStateException("Chỉ hóa đơn PENDING mới có thể được đánh dấu là PAID.");
        }

        invoice.setStatus("PAID");
        invoice.setPaidAt(LocalDateTime.now());
        Invoice savedInvoice = invoiceRepository.save(invoice);

        return invoiceMapper.toResponseDto(savedInvoice);
    }

    /**
     * CHỨC NĂNG 3: Hủy Hóa đơn
     */
    @Transactional
    public InvoiceResponseDTO cancelInvoice(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Không tìm thấy hoá đơn"));

        if (!List.of( "PENDING").contains(invoice.getStatus())) { // <-- Sửa
            throw new IllegalStateException("Chỉ hoá đơn PENDING có thể CANCELLED.");
        }
        invoice.setStatus("CANCELLED");
        Invoice savedInvoice = invoiceRepository.save(invoice);
        return invoiceMapper.toResponseDto(savedInvoice);
    }

    /**
     * CHỨC NĂNG 4: Lấy Hóa đơn theo ID
     */
    @Transactional(readOnly = true)
    public InvoiceResponseDTO getInvoiceById(UUID invoiceId) {
        // Cần join fetch items để lấy luôn
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Không tìm thấy hoá đơn"));

        // Nạp (load) các items
        invoice.getItems().size(); // Thủ thuật để Hibernate load lazy collection
        return invoiceMapper.toResponseDto(invoice);
    }

    /**
     * CHỨC NĂNG 5: Lọc Hóa đơn
     */
    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> listInvoices(String status) {
        if (status != null && !status.isEmpty()) {
            return invoiceMapper.toResponseDtoList(invoiceRepository.findAllByStatus(status));
        }
        return invoiceMapper.toResponseDtoList(invoiceRepository.findAll());
    }

    /**
     * Lấy danh sách invoices theo patientId
     * Query qua appointmentId -> patientId
     */
    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> getInvoicesByPatientId(UUID patientId, String status) {
        log.info("Lấy invoices cho patient: {}", patientId);
        
        try {
            // 1. Gọi appointment-service để lấy danh sách appointments của patient
            List<AppointmentDTO> appointments = appointmentClient.getAppointmentsByPatientId(patientId);
            
            log.info("Tìm thấy {} appointments cho patient {}", appointments.size(), patientId);
            
            // 2. Extract danh sách appointmentIds
            List<UUID> appointmentIds = appointments.stream()
                    .map(AppointmentDTO::getId)
                    .collect(Collectors.toList());
            
            // 3. Nếu không có appointment nào, return empty list
            if (appointmentIds.isEmpty()) {
                log.info("Patient {} không có appointment nào", patientId);
                return List.of();
            }
            
            log.info("Tìm invoices cho {} appointmentIds", appointmentIds.size());
            
            // 4. Query invoices theo appointmentIds (và status nếu có)
            List<Invoice> invoices;
            if (status != null && !status.isEmpty()) {
                invoices = invoiceRepository.findAllByAppointmentIdInAndStatusOrderByIssueAtDesc(
                    appointmentIds, status
                );
            } else {
                invoices = invoiceRepository.findAllByAppointmentIdInOrderByIssueAtDesc(
                    appointmentIds
                );
            }
            
            log.info("Tìm thấy {} invoices cho patient {}", invoices.size(), patientId);
            
            // 5. Map sang DTO và return
            return invoices.stream()
                    .map(invoice -> {
                        // Load items nếu cần
                        invoice.getItems().size();
                        return invoiceMapper.toResponseDto(invoice);
                    })
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Lỗi khi lấy invoices cho patient {}: {}", patientId, e.getMessage(), e);
            throw new RuntimeException("Không thể lấy danh sách hóa đơn của bệnh nhân: " + e.getMessage(), e);
        }
    }

    @Override
    public List<InvoiceResponseDTO> getInvoicesByAppointmentId(UUID appointmentId){
        List<Invoice> invoices = invoiceRepository.findAllByAppointmentId(appointmentId);
        return invoiceMapper.toResponseDtoList(invoices);
    }

    // ==================== NEW METHODS FOR REFACTORING ====================

    @Override
    @Transactional
    public InvoiceResponseDTO addMedicineCharges(UUID invoiceId, Set<InvoiceItemCheckerRequest> items, 
                                                List<MedicineItem> medicineItems) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Không tìm thấy hóa đơn: " + invoiceId));

        int addedAmount = 0;

        for (InvoiceItemCheckerRequest medicineItem : items) {
            InvoiceItem invoiceItem = new InvoiceItem();
            invoiceItem.setId(medicineItem.getId());
            invoiceItem.setServiceType("MEDICINE");
            invoiceItem.setReferenceId(medicineItem.getReferenceId());
            invoiceItem.setQuantity(medicineItem.getQuantity());
            invoiceItem.setDescription(medicineItem.getDescription());
            invoiceItem.setUnitPrice(medicineItem.getUnitPrice());
            invoiceItem.setInsurancePayAmount(0);
            invoiceItem.setPatientPayAmount(medicineItem.getQuantity() * medicineItem.getUnitPrice());

            // Match description from medicineItems
            for (MedicineItem it : medicineItems) {
                if (medicineItem.getId().equals(it.getId()) ||
                        it.getMedicineId().equals(medicineItem.getReferenceId())) {
                    invoiceItem.setDescription(it.getName());
                    break;
                }
            }

            invoice.addItem(invoiceItem);
            invoiceItemRepository.save(invoiceItem);
            addedAmount += (medicineItem.getQuantity() * medicineItem.getUnitPrice());
        }

        int currentTotal = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : 0;
        int currentPatientPay = invoice.getPatientTotalPay() != null ? invoice.getPatientTotalPay() : 0;

        invoice.setPatientTotalPay(currentPatientPay + addedAmount);
        invoice.setTotalAmount(currentTotal + addedAmount);

        Invoice saved = invoiceRepository.save(invoice);
        saved.getItems().size(); // Load items
        return invoiceMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public InvoiceResponseDTO applyInsuranceDiscount(UUID invoiceId, UUID insuranceClaimId, 
                                                    Integer discountAmount, Set<InvoiceItemResponse> items) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Không tìm thấy hóa đơn: " + invoiceId));

        Integer currentTotal = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : 0;
        Integer discount = discountAmount != null ? discountAmount : 0;
        Integer finalAmount = Math.max(0, currentTotal - discount);

        invoice.setInsuranceTotalPay(discount);
        invoice.setPatientTotalPay(finalAmount);
        invoice.setInsuranceClaimId(insuranceClaimId);

        invoiceRepository.save(invoice);

        // Update invoice items
        List<InvoiceItem> existingItems = invoiceItemRepository.findByInvoiceId(invoiceId);
        Map<UUID, InvoiceItem> existingMap = existingItems.stream()
                .collect(Collectors.toMap(InvoiceItem::getId, item -> item));
        List<InvoiceItem> itemsToUpdate = new ArrayList<>();

        for (InvoiceItemResponse it : items) {
            if (existingMap.containsKey(it.getId())) {
                InvoiceItem entity = existingMap.get(it.getId());
                invoiceItemMapper.updateFromResponse(it, entity);
                itemsToUpdate.add(entity);
            }
        }

        invoiceItemRepository.saveAll(itemsToUpdate);

        Invoice saved = invoiceRepository.findById(invoiceId).get();
        saved.getItems().size();
        return invoiceMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public InvoiceResponseDTO revertInsuranceDiscount(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        invoice.setInsuranceTotalPay(0);
        invoice.setPatientTotalPay(invoice.getTotalAmount());
        invoice.setInsuranceClaimId(null);

        Invoice saved = invoiceRepository.save(invoice);
        saved.getItems().size();
        return invoiceMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public InvoiceResponseDTO removeMedicineCharges(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoá đơn"));


        // Delete all medicine items
        invoiceItemRepository.deleteByInvoice_IdAndServiceType(invoiceId, "MEDICINE");

        // Recalculate totals
        List<InvoiceItem> remainingItems = invoiceItemRepository.findByInvoiceId(invoiceId);
        int newTotal = remainingItems.stream()
                .mapToInt(item -> item.getQuantity() * item.getUnitPrice())
                .sum();

        invoice.setTotalAmount(Math.max(0, newTotal));
        invoice.setPatientTotalPay(Math.max(0, newTotal));


        Invoice saved = invoiceRepository.save(invoice);
        saved.getItems().size();
        return invoiceMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public InvoiceResponseDTO updateStatus(UUID invoiceId, String status) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException("Không tìm thấy hóa đơn: " + invoiceId));

        invoice.setStatus(status);
        if ("PAID".equals(status)) {
            invoice.setPaidAt(LocalDateTime.now());
        }

        Invoice saved = invoiceRepository.save(invoice);
        saved.getItems().size();
        return invoiceMapper.toResponseDto(saved);
    }

    // Validation methods
    @Override
    @Transactional(readOnly = true)
    public boolean canAddMedicineCharges(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
        return invoice != null && "PENDING".equals(invoice.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canApplyInsuranceDiscount(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
        return invoice != null && "PENDING".equals(invoice.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCancel(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
        return invoice != null && !"PAID".equals(invoice.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canMarkAsPaid(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
        return invoice != null && !"PAID".equals(invoice.getStatus()) && !"CANCELLED".equals(invoice.getStatus());
    }

    @Override
    @Transactional
    public InvoiceResponseDTO addLabTestCharge(com.do_an.invoiceservice.dto.request.AddLabTestChargeRequestDTO request) {
        log.info("=== Adding Lab Test Charge ===");
        log.info("Request details: labTestId={}, appointmentId={}, price={}",
                request.getLabTestId(), request.getAppointmentId(), request.getPrice());

        List<Invoice> invoices = invoiceRepository.findAllByAppointmentId(request.getAppointmentId());
        if (invoices.isEmpty()) {
            throw new InvoiceNotFoundException("Không tìm thấy hoá đơn cho appointment: " + request.getAppointmentId());
        }

        Invoice invoice = invoices.stream()
                .filter(inv -> "PENDING".equals(inv.getStatus()))
                .findFirst()
                .orElse(invoices.get(0));

        InvoiceItem item = new InvoiceItem();
        item.setId(UUID.randomUUID());
        item.setReferenceId(request.getLabTestId());
        item.setServiceType("LABTEST");
        item.setQuantity(1);
        item.setUnitPrice(request.getPrice());
        item.setInsurancePayAmount(0);
        item.setPatientPayAmount(request.getPrice());
        item.setDescription(request.getDescription() != null ? request.getDescription() : "Phí xét nghiệm");
        item.setInvoice(invoice);
        
        invoice.addItem(item);
        invoiceItemRepository.save(item);

        int currentTotal = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : 0;
        int currentPatientPay = invoice.getPatientTotalPay() != null ? invoice.getPatientTotalPay() : 0;
        int newTotal = currentTotal + request.getPrice();
        int newPatientPay = currentPatientPay + request.getPrice();
        
        invoice.setTotalAmount(newTotal);
        invoice.setPatientTotalPay(newPatientPay);
        invoiceRepository.save(invoice);
        
        log.info("Đã thêm phí xét nghiệm LABTEST vào hoá đơn cho appointment {}, tổng tiền {}",
                request.getAppointmentId(), newTotal);
        
        invoice.getItems().size();
        return invoiceMapper.toResponseDto(invoice);
    }


}