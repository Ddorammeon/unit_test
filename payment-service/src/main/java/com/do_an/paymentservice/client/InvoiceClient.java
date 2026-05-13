package com.do_an.paymentservice.client;

import com.do_an.paymentservice.dto.response.InvoiceResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
    name = "invoice-service",
    url = "${invoice.service.url:http://localhost:8091}"
)
public interface InvoiceClient {
    
    /**
     * Lấy thông tin Invoice theo ID
     * GET /api/invoices/{id}
     */
    @GetMapping("/invoice-service/invoices/{id}")
    InvoiceResponseDTO getInvoiceById(@PathVariable("id") UUID invoiceId);
    
    /**
     * Đánh dấu Invoice đã thanh toán
     * PATCH /api/invoices/{id}/pay
     */
    @PatchMapping("/invoice-service/invoices/{id}/pay")
    InvoiceResponseDTO markAsPaid(@PathVariable("id") UUID invoiceId);
    
    /**
     * Hủy Invoice
     * PATCH /api/invoices/{id}/cancel
     */
    @PatchMapping("/invoice-service/invoices/{id}/cancel")
    InvoiceResponseDTO cancelInvoice(@PathVariable("id") UUID invoiceId);


}
