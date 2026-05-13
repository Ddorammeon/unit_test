package com.do_an.paymentservice.controller;

import com.do_an.paymentservice.dto.request.CreatePaymentRequestDTO;
import com.do_an.paymentservice.dto.request.UpdatePaymentRequestDTO;
import com.do_an.paymentservice.dto.response.PaymentResponseDTO;
import com.do_an.paymentservice.entity.PaymentMethod;
import com.do_an.paymentservice.entity.PaymentStatus;
import com.do_an.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payment-service/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "API quản lý thanh toán hóa đơn. Hỗ trợ thanh toán tiền mặt và chuyển khoản qua PayOS.")
public class PaymentController {

    private final PaymentService paymentService;

    //Dùng
    @Operation(
            summary = "Khởi tạo thanh toán",
            description = "Tạo một payment mới cho hóa đơn. Tổng tiền sẽ được tính tự động từ InvoiceItem của Invoice. " +
                    "Hỗ trợ thanh toán tiền mặt (CASH) và chuyển khoản ngân hàng (BANK_TRANSFER)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo payment thành công",
                    content = @Content(schema = @Schema(implementation = PaymentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ hoặc Invoice không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy Invoice"),
            @ApiResponse(responseCode = "500", description = "Lỗi server hoặc lỗi khi gọi PayOS")
    })
    @PostMapping
    public ResponseEntity<PaymentResponseDTO> initiatePayment(
            @Parameter(description = "Thông tin thanh toán. totalAmount là optional, sẽ được tính từ InvoiceItem nếu không có.", required = true)
            @Valid @RequestBody CreatePaymentRequestDTO request) {

        log.info("Nhận request thanh toán từ client");
        PaymentResponseDTO response = paymentService.initiatePayment(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    //Dùng
    @Operation(
            summary = "Lấy trạng thái thanh toán",
            description = "Lấy trạng thái thanh toán mới nhất của một Invoice. Dùng cho client polling."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm thấy payment",
                    content = @Content(schema = @Schema(implementation = PaymentResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy payment cho Invoice này")
    })
    @GetMapping("/status")
    public ResponseEntity<PaymentResponseDTO> getPaymentStatusOfInvoice(
            @Parameter(description = "ID của Invoice", required = true)
            @RequestParam UUID invoiceId) {

        log.info("Kiểm tra trạng thái thanh toán cho Invoice: {}", invoiceId);
        PaymentResponseDTO response = paymentService.getPaymentStatusOfInvoice(invoiceId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Lấy chi tiết payment",
            description = "Lấy thông tin chi tiết của một payment theo Payment ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm thấy payment",
                    content = @Content(schema = @Schema(implementation = PaymentResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy payment")
    })
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(
            @Parameter(description = "ID của Payment", required = true)
            @PathVariable UUID paymentId) {

        log.info("Lấy chi tiết Payment: {}", paymentId);
        PaymentResponseDTO response = paymentService.getPaymentById(paymentId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Cập nhật payment",
            description = "Cập nhật thông tin payment. Không thể thay đổi trạng thái của payment đã thành công."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy payment"),
            @ApiResponse(responseCode = "409", description = "Không thể cập nhật payment đã thành công")
    })
    @PutMapping("/{paymentId}")
    public ResponseEntity<PaymentResponseDTO> updatePayment(
            @Parameter(description = "ID của Payment", required = true)
            @PathVariable UUID paymentId,
            @Parameter(description = "Thông tin cần cập nhật", required = true)
            @Valid @RequestBody UpdatePaymentRequestDTO request) {
        
        log.info("Cập nhật Payment: {}", paymentId);
        PaymentResponseDTO response = paymentService.updatePayment(paymentId, request);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Xóa payment",
            description = "Xóa payment. Chỉ có thể xóa payment chưa thành công (không phải SUCCESSFUL)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy payment"),
            @ApiResponse(responseCode = "409", description = "Không thể xóa payment đã thành công")
    })
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<Void> deletePayment(
            @Parameter(description = "ID của Payment", required = true)
            @PathVariable UUID paymentId) {
        
        log.info("Xóa Payment: {}", paymentId);
        paymentService.deletePayment(paymentId);
        
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Lấy danh sách payments",
            description = "Lấy danh sách payments với các filter: invoiceId, status, paymentMethod"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    @GetMapping
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments(
            @Parameter(description = "ID của Invoice (optional)")
            @RequestParam(required = false) UUID invoiceId,
            @Parameter(description = "Trạng thái payment: PENDING, SUCCESSFUL, FAILED, CANCELLED, REFUNDED (optional)")
            @RequestParam(required = false) PaymentStatus status,
            @Parameter(description = "Phương thức thanh toán: CASH, BANK_TRANSFER (optional)")
            @RequestParam(required = false) PaymentMethod paymentMethod) {
        
        log.info("Lấy danh sách payments với filters - InvoiceId: {}, Status: {}, Method: {}", 
                invoiceId, status, paymentMethod);
        
        List<PaymentResponseDTO> response = paymentService.getAllPayments(
                invoiceId, status, paymentMethod);
        
        return ResponseEntity.ok(response);
    }


    //Dùng
    @Operation(
            summary = "Lấy tất cả payments theo Invoice ID",
            description = "Lấy tất cả payments của một Invoice (có thể có nhiều payment cho một Invoice)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByInvoiceId(
            @Parameter(description = "ID của Invoice", required = true)
            @PathVariable UUID invoiceId) {
        
        log.info("Lấy tất cả payments cho Invoice: {}", invoiceId);
        List<PaymentResponseDTO> response = paymentService.getPaymentsByInvoiceId(invoiceId);
        
        return ResponseEntity.ok(response);
    }


    //Dùng
    @Operation(
        summary = "Xử lý callback từ PayOS redirect",
        description = "Endpoint này được gọi từ frontend sau khi PayOS redirect về. Dùng để cập nhật trạng thái payment cho cả trường hợp CANCELLED."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy payment")
    })
    @PostMapping("/callback")
    public ResponseEntity<PaymentResponseDTO> handlePaymentCallback(
        @Parameter(description = "Order code từ PayOS", required = true)
        @RequestParam Long orderCode,
        @Parameter(description = "Status từ PayOS: PAID, CANCELLED, etc.")
        @RequestParam(required = false) String status,
        @Parameter(description = "Code từ PayOS: 00 = success")
        @RequestParam(required = false) String code,
        @Parameter(description = "Cancel flag")
        @RequestParam(required = false) Boolean cancel) {
    
        log.info("Nhận callback từ PayOS - OrderCode: {}, Status: {}, Code: {}, Cancel: {}", 
                orderCode, status, code, cancel);
        
        PaymentResponseDTO response = paymentService.handlePaymentCallback(
                String.valueOf(orderCode), status, code, cancel);
        
        return ResponseEntity.ok(response);
    }
}