package com.do_an.invoiceservice.controller;

import com.do_an.common.command.CancelInvoiceCommand;
import com.do_an.invoiceservice.dto.request.CreateInvoiceRequestDTO;
import com.do_an.invoiceservice.dto.response.InvoiceResponseDTO;
import com.do_an.invoiceservice.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.do_an.invoiceservice.repository.InvoiceRepository;
import com.do_an.invoiceservice.mapper.InvoiceMapper;

@RestController
@RequestMapping("/invoice-service/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoice Management", description = "API quản lý hóa đơn và chi tiết hóa đơn")
public class InvoiceController {
    private static final Logger log = LoggerFactory.getLogger(InvoiceController.class);

    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;

    private final CommandGateway commandGateway;

    @Operation(
            summary = "Tạo hóa đơn mới",
            description = "Tạo một hóa đơn mới với các InvoiceItem. Hóa đơn sẽ được tạo với trạng thái PENDING."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Hóa đơn được tạo thành công",
                    content = @Content(schema = @Schema(implementation = InvoiceResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> createInvoice(
            @Parameter(description = "Thông tin hóa đơn cần tạo", required = true)
            @Valid @RequestBody CreateInvoiceRequestDTO request) {
        InvoiceResponseDTO newInvoice = invoiceService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newInvoice);
    }

    @Operation(
            summary = "Cập nhật hóa đơn",
            description = "Cập nhật hóa đơn. Chỉ có thể cập nhật khi hóa đơn ở trạng thái PENDING."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = InvoiceResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hóa đơn"),
            @ApiResponse(responseCode = "409", description = "Hóa đơn không ở trạng thái PENDING")
    })
    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> updateInvoice(
            @Parameter(description = "ID của hóa đơn cần cập nhật", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Thông tin hóa đơn cần cập nhật", required = true)
            @Valid @RequestBody CreateInvoiceRequestDTO request) {
        InvoiceResponseDTO updatedInvoice = invoiceService.updateInvoice(id, request);
        return ResponseEntity.ok(updatedInvoice);
    }


    //Dùng
    @Operation(
            summary = "Lấy hóa đơn theo ID",
            description = "Lấy thông tin chi tiết của một hóa đơn bao gồm các InvoiceItem"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm thấy hóa đơn",
                    content = @Content(schema = @Schema(implementation = InvoiceResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hóa đơn")
    })
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceById(
            @Parameter(description = "ID của hóa đơn", required = true)
            @PathVariable UUID id) {
        InvoiceResponseDTO invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }

    //Dùng
    @Operation(
            summary = "Lấy danh sách hóa đơn",
            description = "Lấy danh sách hóa đơn với filter theo trạng thái (optional)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    @GetMapping
    public ResponseEntity<List<InvoiceResponseDTO>> listInvoices(
            @Parameter(description = "Trạng thái hóa đơn (PENDING, PAID, CANCELLED)", required = false)
            @RequestParam(required = false) String status) {
        List<InvoiceResponseDTO> invoices = invoiceService.listInvoices(status);
        return ResponseEntity.ok(invoices);
    }

    //Dùng
    @Operation(
            summary = "Lấy danh sách hóa đơn theo Patient ID",
            description = "Lấy tất cả hóa đơn của một bệnh nhân cụ thể"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoicesByPatientId(
            @Parameter(description = "ID của bệnh nhân", required = true)
            @PathVariable UUID patientId,
            @Parameter(description = "Trạng thái hóa đơn (optional)")
            @RequestParam(required = false) String status) {

        log.info("Lấy danh sách hóa đơn cho patient: {}, status: {}", patientId, status);

        List<InvoiceResponseDTO> invoices = invoiceService.getInvoicesByPatientId(patientId, status);

        return ResponseEntity.ok(invoices);
    }

    @Operation(
            summary = "Đánh dấu hóa đơn đã thanh toán",
            description = "Chuyển trạng thái hóa đơn từ PENDING sang PAID. Chỉ có thể thực hiện khi hóa đơn ở trạng thái PENDING."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đánh dấu thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hóa đơn"),
            @ApiResponse(responseCode = "409", description = "Hóa đơn không ở trạng thái PENDING")
    })

    @PatchMapping("/{id}/pay")
    public ResponseEntity<InvoiceResponseDTO> markAsPaid(
            @Parameter(description = "ID của hóa đơn", required = true)
            @PathVariable UUID id) {
        InvoiceResponseDTO paidInvoice = invoiceService.markAsPaid(id);
        return ResponseEntity.ok(paidInvoice);
    }


    @Operation(
            summary = "Hủy hóa đơn",
            description = "Chuyển trạng thái hóa đơn sang CANCELLED. Chỉ có thể hủy khi hóa đơn ở trạng thái PENDING."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hủy hóa đơn thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hóa đơn"),
            @ApiResponse(responseCode = "409", description = "Không thể hủy hóa đơn ở trạng thái hiện tại")
    })

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<InvoiceResponseDTO> cancelInvoice(
            @Parameter(description = "ID của hóa đơn", required = true)
            @PathVariable UUID id) {
        InvoiceResponseDTO cancelledInvoice = invoiceService.cancelInvoice(id);
        return ResponseEntity.ok(cancelledInvoice);
    }

    //Dùng
    @PostMapping("/{id}/cancel")
    public CompletableFuture<String> cancelInvoice(@PathVariable UUID id, @RequestParam String reason) {
        return commandGateway.send(new CancelInvoiceCommand(id, reason));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoicesByAppointmentId(
            @PathVariable UUID appointmentId) {
        List<InvoiceResponseDTO> invoices = invoiceService.getInvoicesByAppointmentId(appointmentId);
        return ResponseEntity.ok(invoices);
    }

    @Operation(
            summary = "Thêm phí xét nghiệm vào hóa đơn",
            description = "Thêm phí xét nghiệm (LABTEST) vào hóa đơn của appointment. Tự động tìm hóa đơn theo appointmentId."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thêm phí thành công",
                    content = @Content(schema = @Schema(implementation = InvoiceResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hóa đơn cho appointment"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ")
    })
    @PostMapping("/labtest-charge")
    public ResponseEntity<InvoiceResponseDTO> addLabTestCharge(
            @Parameter(description = "Thông tin phí xét nghiệm cần thêm", required = true)
            @Valid @RequestBody com.do_an.invoiceservice.dto.request.AddLabTestChargeRequestDTO request) {
        InvoiceResponseDTO updatedInvoice = invoiceService.addLabTestCharge(request);
        return ResponseEntity.ok(updatedInvoice);
    }
    

}
