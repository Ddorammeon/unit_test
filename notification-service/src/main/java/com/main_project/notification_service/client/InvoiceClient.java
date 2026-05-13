package com.main_project.notification_service.client;

import com.main_project.notification_service.dto.response.InvoiceResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "invoice-service",
        url = "${invoice.service.url:http://localhost:8091}"
)
public interface InvoiceClient {
    @GetMapping("/invoice-service/invoices/appointment/{appointmentId}")
    List<InvoiceResponseDTO> getInvoicesByAppointmentId(@PathVariable UUID appointmentId);
}

