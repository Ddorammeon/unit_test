package com.do_an.paymentservice.service;

import com.do_an.common.command.CreatePaymentCommand;
import com.do_an.common.command.UpdatePaymentStatusCommand;
import com.do_an.common.event.PaymentFailedEvent;
import com.do_an.common.event.PaymentProcessedEvent;
import com.do_an.paymentservice.client.InventoryClient;
import com.do_an.paymentservice.client.InvoiceClient;
import com.do_an.paymentservice.client.PatientClient;
import com.do_an.paymentservice.dto.request.CreatePaymentRequestDTO;
import com.do_an.paymentservice.dto.request.UpdatePaymentRequestDTO;
import com.do_an.paymentservice.dto.response.*;
import com.do_an.paymentservice.dto.response.DispenseOrderResponse;
import com.do_an.paymentservice.entity.Payment;
import com.do_an.paymentservice.entity.PaymentMethod;
import com.do_an.paymentservice.entity.PaymentStatus;
import com.do_an.paymentservice.exception.PaymentNotFoundException;
import com.do_an.paymentservice.iservice.IPaymentService;
import com.do_an.paymentservice.mapper.PaymentMapper;
import com.do_an.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.GenericEventMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final PayOS payOS;
    private final PaymentMapper paymentMapper;
    private final InventoryClient inventoryClient;
    private final InvoiceClient invoiceClient;
    private final PatientClient patientClient;
    private final CommandGateway commandGateway;
    private final EventBus eventBus;

    @Value("${payos.return-url}")
    private String returnUrl;

    @Value("${payos.cancel-url}")
    private String cancelUrl;

    /**
     * CHỨC NĂNG 1: Khởi tạo Thanh toán (CASH hoặc BANK_TRANSFER)
     * Tính tổng tiền từ InvoiceItem của Invoice
     */

    @Transactional
    public PaymentResponseDTO initiatePayment(CreatePaymentRequestDTO request) {
        log.info("Khởi tạo thanh toán cho Invoice: {}, Phương thức: {}", 
                request.getInvoiceId(), request.getPaymentMethod());

        // Lấy thông tin Invoice từ invoice-service (bao gồm danh sách InvoiceItem)
        InvoiceResponseDTO invoice;
        UUID dispenseOrderId = null;
        try {
            invoice = invoiceClient.getInvoiceById(request.getInvoiceId());
            log.info("Đã lấy thông tin Invoice: {}, Status: {}, TotalAmount: {}", 
                    invoice.getId(), invoice.getStatus(), invoice.getTotalAmount());

            // Thử lấy DispenseOrder (có thể không có)
            try {
                MedicalHistoryResponseDTO medicalHistoryResponseDTO = patientClient.getByAppointment(invoice.getAppointmentId()).get(0);
                ResponseEntity<DispenseOrderResponse> responseEntity = inventoryClient.getByMedicalHistoryId(medicalHistoryResponseDTO.getId());
                
                if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                    dispenseOrderId = responseEntity.getBody().getId();
                    log.info("Đã tìm thấy DispenseOrder: {}", dispenseOrderId);
                } else {
                    log.info("Không tìm thấy DispenseOrder cho medicalHistoryId: {}. Tiếp tục thanh toán không có DispenseOrder.", 
                            medicalHistoryResponseDTO.getId());
                }
            } catch (Exception e) {
                log.warn("Lỗi khi lấy DispenseOrder: {}. Tiếp tục thanh toán không có DispenseOrder.", e.getMessage());
                // Tiếp tục thanh toán mà không có DispenseOrder
            }
            
            // Kiểm tra Invoice có items không
            if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
                throw new IllegalStateException("Invoice không có items. Không thể thanh toán.");
            }
            
            // Kiểm tra trạng thái Invoice
            if (!List.of("DRAFT", "PENDING").contains(invoice.getStatus())) {
                throw new IllegalStateException("Không thể thanh toán Invoice với trạng thái: " + invoice.getStatus());
            }
            
            // Tính tổng tiền từ InvoiceItem
            //int calculatedTotalAmount = calculateTotalAmountFromItems(invoice.getItems());

            int calculatedTotalAmount = invoice.getPatientTotalPay();

            log.info("Tổng tiền tính từ InvoiceItem: {}", calculatedTotalAmount);
            
            // Nếu client gửi totalAmount, kiểm tra khớp
            if (request.getTotalAmount() != null) {
                if (!request.getTotalAmount().equals(calculatedTotalAmount)) {
                    log.warn("Số tiền từ client ({}) không khớp với tổng tiền tính từ InvoiceItem ({})", 
                            request.getTotalAmount(), calculatedTotalAmount);
                    // Có thể throw exception hoặc chỉ log warning tùy business logic
                    // throw new IllegalArgumentException("Số tiền không khớp với hóa đơn");
                }
            }
            
            // Cập nhật totalAmount từ InvoiceItem vào request
            request.setTotalAmount(calculatedTotalAmount);
            
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin Invoice: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lấy thông tin Invoice: " + e.getMessage(), e);
        }

        if (request.getPaymentMethod() == PaymentMethod.CASH) {
            return handleCashPayment(request, invoice, dispenseOrderId);
        } else {
            return handleBankTransferPayment(request, invoice, dispenseOrderId);
        }
    }

    /**
     * Tính tổng tiền từ danh sách InvoiceItem
     */
    private int calculateTotalAmountFromItems(List<InvoiceItemResponseDTO> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("Invoice không có items để tính tổng tiền");
        }
        
        return items.stream()
                .mapToInt(item -> {
                    // Sử dụng itemTotal nếu có, nếu không thì tính từ quantity * unitPrice
                    if (item.getItemTotal() != null) {
                        return item.getItemTotal();
                    } else if (item.getQuantity() != null && item.getUnitPrice() != null) {
                        return item.getQuantity() * item.getUnitPrice();
                    } else {
                        log.warn("InvoiceItem {} không có đủ thông tin để tính tổng tiền", item.getId());
                        return 0;
                    }
                })
                .sum();
    }

    /**
     * Xử lý thanh toán tiền mặt
     */
    @Transactional
    public PaymentResponseDTO handleCashPayment(CreatePaymentRequestDTO request, InvoiceResponseDTO invoice, UUID dispenseOrderId) {
        log.info("Xử lý thanh toán CASH cho Invoice: {}", request.getInvoiceId());

        Payment payment = Payment.builder()
                .invoiceId(request.getInvoiceId())
                .totalAmount(request.getTotalAmount())
                .paymentMethod(PaymentMethod.CASH)
                .status(PaymentStatus.PENDING)
                .paidAt(LocalDateTime.now())
                .description("Thanh toán tiền mặt")
                .build();

        paymentRepository.save(payment);


        // GỬI LỆNH ĐỂ ĐỒNG BỘ TRẠNG THÁI QUA AXON (Dù đã thành công)
        // Việc này giúp Saga nhận biết và có thể bắn event notification thống nhất
        commandGateway.sendAndWait(new CreatePaymentCommand(
                payment.getId(),
                payment.getInvoiceId(),
                dispenseOrderId,
                payment.getTotalAmount()
        ));

        // Vì tiền mặt là thành công ngay, ta gửi tiếp lệnh Update Status luôn
        // Hoặc để PaymentAggregate tự xử lý nếu logic của bạn cho phép
        commandGateway.send(new UpdatePaymentStatusCommand(
                payment.getId(),
                "SUCCESSFUL",
                "Đã thanh toán đầy đủ tiền mặt"
        ));

        // Báo cho Invoice Service - Đánh dấu đã thanh toán
//        try {
//            InvoiceResponseDTO updatedInvoice = invoiceClient.markAsPaid(request.getInvoiceId());
//            log.info("Đã cập nhật Invoice {} thành PAID", request.getInvoiceId());
//        } catch (Exception e) {
//            log.error("Lỗi khi gọi Invoice Service để đánh dấu đã thanh toán: {}", e.getMessage(), e);
//        }

        return paymentMapper.toResponseDto(payment);
    }

    /**
     * Xử lý thanh toán chuyển khoản (payOS)
     * Sử dụng InvoiceItem để tạo ItemData chi tiết cho payOS
     */
    @Transactional
    public PaymentResponseDTO handleBankTransferPayment(CreatePaymentRequestDTO request, InvoiceResponseDTO invoice, UUID dispenserOrderId) {
        log.info("Xử lý thanh toán BANK_TRANSFER cho Invoice: {}", request.getInvoiceId());

        try {
            // Tạo orderCode duy nhất
            Long orderCode = System.currentTimeMillis();

            // Tạo danh sách ItemData từ InvoiceItem
            List<ItemData> payosItems = convertInvoiceItemsToPayOSItems(invoice.getItems());
            
            log.info("Đã chuyển đổi {} InvoiceItem thành PayOS ItemData", payosItems.size());

            // Tính tổng tiền từ items
//            int totalAmountInt = payosItems.stream()
//                    .mapToInt(item -> item.getPrice() * item.getQuantity())
//                    .sum();

            String invoiceId = request.getInvoiceId().toString();
            invoiceId = invoiceId.substring(invoiceId.length() - 17);

            // Chuẩn bị dữ liệu thanh toán cho payOS
            LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(15);
            long expiredAtUnix = (System.currentTimeMillis() / 1000) + 900;
            PaymentData paymentData = PaymentData.builder()
                    .orderCode(orderCode)
                    .amount(request.getTotalAmount())
                    .description("Hoa don: " + invoiceId)
                    .items(payosItems)
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .expiredAt(expiredAtUnix) // 15 minute expiry
                    .build();

            // Gọi payOS SDK để tạo link thanh toán
            CheckoutResponseData payosResponse = payOS.createPaymentLink(paymentData);
            log.info("Tạo payment link thành công: {}", payosResponse.getCheckoutUrl());

            // Lưu payment record
            Payment payment = Payment.builder()
                    .invoiceId(request.getInvoiceId())
                    .totalAmount(request.getTotalAmount())
                    .paymentMethod(PaymentMethod.BANK_TRANSFER)
                    .status(PaymentStatus.PENDING)
                    .transactionId(String.valueOf(orderCode))
                    .paymentUrl(payosResponse.getCheckoutUrl())
                    .description("Chờ khách hàng chuyển khoản")
                    .expiredAt(expiredAt)
                    .build();

            Payment paymentSaved =  paymentRepository.save(payment);

            commandGateway.send(new CreatePaymentCommand(
                    payment.getId(),          // ID của Payment vừa tạo
                    payment.getInvoiceId(),   // ID hóa đơn
                    dispenserOrderId,
                    payment.getTotalAmount()
            ));



            log.info("--> Đã gửi CreatePaymentCommand để kích hoạt PaymentSaga. ID: {}", payment.getId());

            return paymentMapper.toResponseDto(paymentSaved);

        } catch (Exception e) {
            log.error("Lỗi khi tạo link thanh toán payOS: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi tạo link thanh toán: " + e.getMessage(), e);
        }
    }

    /**
     * Chuyển đổi InvoiceItem thành PayOS ItemData
     */
    private List<ItemData> convertInvoiceItemsToPayOSItems(List<InvoiceItemResponseDTO> invoiceItems) {
        if (invoiceItems == null || invoiceItems.isEmpty()) {
            throw new IllegalStateException("Invoice không có items");
        }
        
        return invoiceItems.stream()
                .map(item -> {
                    // Tính giá từ itemTotal hoặc quantity * unitPrice
                    int itemPrice = item.getUnitPrice();

                    // Tên item: serviceType hoặc description
                    String serviceType = item.getServiceType();
                    String description = item.getDescription() != null ? item.getDescription() : "";

                    String itemName;

                    switch (serviceType) {
                        case "Medicine":
                            itemName = "Thuốc " + description;
                            break;
                        case "Dental":
                            itemName = "Dịch vụ y tế " + description;
                            break;
                        default:
                            if (serviceType != null && !serviceType.isEmpty()) {
                                itemName = serviceType; // serviceType khác Medicine/Dental
                            } else {
                                itemName = description.isEmpty() ? "Dịch vụ y tế" : description; // fallback
                            }
                            break;
                    }


                    return ItemData.builder()
                            .name(itemName)
                            .quantity(item.getQuantity() != null ? item.getQuantity() : 1)
                            .price(itemPrice)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * CHỨC NĂNG 2: Xử lý Webhook từ payOS
     */
    @Transactional
    public void handlePayOSWebhook(String transactionId, boolean isSuccess, String bankTransactionId) {
        log.info("Xử lý Webhook từ payOS - Transaction ID: {}, Success: {}", transactionId, isSuccess);

        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentNotFoundException("Không tìm thấy payment với Transaction ID: " + transactionId));

        // Kiểm tra xem payment có đang ở trạng thái chờ không (tránh xử lý trùng)
        if (payment.getStatus() == PaymentStatus.PENDING) {

            PaymentStatus newStatus = isSuccess ? PaymentStatus.SUCCESSFUL : PaymentStatus.FAILED;
            String reason = isSuccess ? "Thanh toán qua payOS - Thành công" : "Thanh toán thất bại";



            // 1. CẬP NHẬT DB TRỰC TIẾP (Để response nhanh cho Webhook)
//            payment.setStatus(newStatus);
//            if (isSuccess) {
//                payment.setPaidAt(LocalDateTime.now());
//            }
//            payment.setDescription(reason);
//            paymentRepository.save(payment);

            // 2. GỬI LỆNH UPDATE STATUS VÀO AXON (KÍCH HOẠT SAGA)
            // Đây là phần BỔ SUNG QUAN TRỌNG
            commandGateway.send(new UpdatePaymentStatusCommand(
                    payment.getId(),
                    newStatus.toString(),
                    reason
            ));

            log.info("--> Đã gửi UpdatePaymentStatusCommand ({}) cho Payment Saga. ID: {}", newStatus, payment.getId());

            // 3. (Optional) Gọi trực tiếp Invoice Service nếu muốn fail-safe (nhưng nên để Saga làm)
            /*
            if (isSuccess) {
                try {
                    invoiceClient.markAsPaid(payment.getInvoiceId());
                } catch (Exception e) {
                    log.error("Lỗi gọi Invoice Service direct call: {}", e.getMessage());
                }
            }
            */
        } else {
            log.warn("Payment {} đã được xử lý trước đó (Status: {}). Bỏ qua.", transactionId, payment.getStatus());
        }
    }

    /**
     * CHỨC NĂNG 3: Lấy trạng thái thanh toán (Client polling)
     */
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentStatusOfInvoice(UUID invoiceId) {
        log.debug("Lấy trạng thái thanh toán cho Invoice: {}", invoiceId);

        Payment payment = paymentRepository.findFirstByInvoiceIdOrderByCreateAtDesc(invoiceId)
                .orElseThrow(() -> new PaymentNotFoundException("Không tìm thấy thanh toán cho Invoice: " + invoiceId));

        return paymentMapper.toResponseDto(payment);
    }

    /**
     * Lấy chi tiết payment theo ID
     */
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentById(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Không tìm thấy Payment ID: " + paymentId));

        return paymentMapper.toResponseDto(payment);
    }

    /**
     * CHỨC NĂNG 4: Cập nhật Payment
     */
    @Transactional
    public PaymentResponseDTO updatePayment(UUID paymentId, UpdatePaymentRequestDTO request) {
        log.info("Cập nhật Payment: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Không tìm thấy Payment ID: " + paymentId));
        
        // Kiểm tra nếu payment đã thành công thì không cho phép cập nhật một số trường
        if (payment.getStatus() == PaymentStatus.SUCCESSFUL && request.getStatus() != null) {
            log.warn("Không thể thay đổi trạng thái của payment đã thành công");
            throw new IllegalStateException("Không thể thay đổi trạng thái của payment đã thành công");
        }
        
        // Cập nhật các trường
        if (request.getTotalAmount() != null) {
            // Nếu cập nhật totalAmount, validate với Invoice
            try {
                InvoiceResponseDTO invoice = invoiceClient.getInvoiceById(payment.getInvoiceId());
                int calculatedTotal = calculateTotalAmountFromItems(invoice.getItems());
                if (!request.getTotalAmount().equals(calculatedTotal)) {
                    log.warn("Số tiền cập nhật ({}) không khớp với tổng tiền InvoiceItem ({})", 
                            request.getTotalAmount(), calculatedTotal);
                }
            } catch (Exception e) {
                log.warn("Không thể validate totalAmount với Invoice: {}", e.getMessage());
            }
            payment.setTotalAmount(request.getTotalAmount());
        }
        
        if (request.getStatus() != null) {
            payment.setStatus(request.getStatus());
            if (request.getStatus() == PaymentStatus.SUCCESSFUL && payment.getPaidAt() == null) {
                payment.setPaidAt(LocalDateTime.now());
                
                // Cập nhật Invoice nếu payment thành công
                try {
                    invoiceClient.markAsPaid(payment.getInvoiceId());
                    log.info("Đã cập nhật Invoice {} thành PAID", payment.getInvoiceId());
                } catch (Exception e) {
                    log.error("Lỗi khi gọi Invoice Service: {}", e.getMessage(), e);
                }
            }
        }
        
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            payment.setDescription(request.getDescription());
        }
        
        paymentRepository.save(payment);
        
        log.info("Đã cập nhật Payment: {}", paymentId);
        return paymentMapper.toResponseDto(payment);
    }

    /**
     * CHỨC NĂNG 5: Xóa Payment
     */
    @Transactional
    public void deletePayment(UUID paymentId) {
        log.info("Xóa Payment: {}", paymentId);
        
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Không tìm thấy Payment ID: " + paymentId));
        
        // Kiểm tra nếu payment đã thành công thì không cho phép xóa
        if (payment.getStatus() == PaymentStatus.SUCCESSFUL) {
            log.warn("Không thể xóa payment đã thành công");
            throw new IllegalStateException("Không thể xóa payment đã thành công");
        }
        
        paymentRepository.delete(payment);
        log.info("Đã xóa Payment: {}", paymentId);
    }

    /**
     * CHỨC NĂNG 6: Lấy danh sách Payments với lọc (không phân trang)
     */
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getAllPayments(
            UUID invoiceId,
            PaymentStatus status,
            PaymentMethod paymentMethod) {
        
        log.debug("Lấy danh sách payments với filters - InvoiceId: {}, Status: {}, Method: {}", 
                invoiceId, status, paymentMethod);
        
        List<Payment> payments;
        
        // Xử lý các trường hợp filter khác nhau
        if (invoiceId != null && status != null && paymentMethod != null) {
            payments = paymentRepository.findAllByInvoiceIdAndStatusAndPaymentMethod(
                    invoiceId, status, paymentMethod);
        } else if (invoiceId != null && status != null) {
            payments = paymentRepository.findAllByInvoiceIdAndStatus(invoiceId, status);
        } else if (invoiceId != null && paymentMethod != null) {
            payments = paymentRepository.findAllByInvoiceIdAndPaymentMethod(invoiceId, paymentMethod);
        } else if (status != null && paymentMethod != null) {
            payments = paymentRepository.findAllByStatusAndPaymentMethod(status, paymentMethod);
        } else if (invoiceId != null) {
            payments = paymentRepository.findAllByInvoiceId(invoiceId);
        } else if (status != null) {
            payments = paymentRepository.findAllByStatus(status);
        } else if (paymentMethod != null) {
            payments = paymentRepository.findAllByPaymentMethod(paymentMethod);
        } else {
            payments = paymentRepository.findAllByOrderByCreateAtDesc();
        }
        
        return payments.stream()
                .map(paymentMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * CHỨC NĂNG 7: Lấy tất cả payments theo Invoice ID
     */
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByInvoiceId(UUID invoiceId) {
        log.debug("Lấy tất cả payments cho Invoice: {}", invoiceId);
        
        List<Payment> payments = paymentRepository.findAllByInvoiceId(invoiceId);
        
        return payments.stream()
                .map(paymentMapper::toResponseDto)
                .collect(Collectors.toList());
    }
    
    /**
     * CHỨC NĂNG 8: Kiểm tra Invoice tồn tại và hợp lệ
     */
    @Transactional(readOnly = true)
    public InvoiceResponseDTO validateInvoice(UUID invoiceId) {
        log.debug("Kiểm tra Invoice: {}", invoiceId);
        
        try {
            InvoiceResponseDTO invoice = invoiceClient.getInvoiceById(invoiceId);
            
            // Kiểm tra Invoice có items không
            if (invoice.getItems() == null || invoice.getItems().isEmpty()) {
                throw new IllegalStateException("Invoice không có items");
            }
            
            // Tính tổng tiền từ items
            int calculatedTotal = calculateTotalAmountFromItems(invoice.getItems());
            log.info("Invoice hợp lệ: {} - Status: {} - TotalAmount (tính từ items): {}", 
                    invoice.getId(), invoice.getStatus(), calculatedTotal);
            
            return invoice;
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra Invoice: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể lấy thông tin Invoice: " + e.getMessage(), e);
        }
    }

    /**
     * Xử lý callback từ PayOS redirect URL
     * Đặc biệt xử lý trường hợp CANCELLED vì PayOS không gửi webhook
     */
    @Transactional
    public PaymentResponseDTO handlePaymentCallback(String orderCode, String status, String code, Boolean cancel) {
        log.info("Xử lý callback redirect - OrderCode: {}, Status: {}, Code: {}, Cancel: {}", 
                orderCode, status, code, cancel);
        
        Payment payment = paymentRepository.findByTransactionId(orderCode)
                .orElseThrow(() -> new PaymentNotFoundException("Không tìm thấy payment với orderCode: " + orderCode));
        
        // Nếu payment đã xử lý xong thì return luôn
        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.info("Payment đã được xử lý: {}", payment.getStatus());
            return paymentMapper.toResponseDto(payment);
        }
        
        // Xác định trạng thái mới
        PaymentStatus newStatus;
        String reason;
        
        if (Boolean.TRUE.equals(cancel) || "CANCELLED".equals(status)) {
            newStatus = PaymentStatus.CANCELLED;
            reason = "Khách hàng đã hủy thanh toán";
        } else if ("00".equals(code) || "PAID".equals(status)) {
            newStatus = PaymentStatus.SUCCESSFUL;
            reason = "Thanh toán thành công qua PayOS";
        } else {
            newStatus = PaymentStatus.FAILED;
            reason = "Thanh toán thất bại - Code: " + code;
        }
        
        log.info("Cập nhật payment {} từ {} sang {}", payment.getId(), payment.getStatus(), newStatus);
        
        // Gửi command để cập nhật status (trigger Saga)
        commandGateway.send(new UpdatePaymentStatusCommand(
                payment.getId(),
                newStatus.toString(),
                reason
        ));
        
        // Cập nhật local để return ngay
        payment.setStatus(newStatus);
        if (newStatus == PaymentStatus.SUCCESSFUL) {
            payment.setPaidAt(LocalDateTime.now());
        }
        payment.setDescription(reason);
        paymentRepository.save(payment);
        
        return paymentMapper.toResponseDto(payment);
    }

    // ========== Methods for handlers ==========


    @Override
    @Transactional(readOnly = true)
    public boolean canUpdatePaymentStatus(UUID paymentId) {
        PaymentResponseDTO payment = getPaymentById(paymentId);
        // Không thể cập nhật nếu payment đã thành công
        return payment.getStatus() != PaymentStatus.SUCCESSFUL;
    }

    @Override
    @Transactional
    public void updatePaymentStatusFromEvent(UUID paymentId, PaymentStatus status, String reason) {
        log.info("Cập nhật Payment {} từ event - Status: {}", paymentId, status);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Không tìm thấy Payment ID: " + paymentId));

        payment.setStatus(status);
        if (status == PaymentStatus.SUCCESSFUL) {
            payment.setPaidAt(LocalDateTime.now());
        }
        payment.setDescription(reason);
        paymentRepository.save(payment);

        log.info("Đã cập nhật DB: Payment {} với status {}", paymentId, status);
    }

}

