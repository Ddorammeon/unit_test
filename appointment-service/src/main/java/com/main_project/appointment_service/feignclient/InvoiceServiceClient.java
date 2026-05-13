package com.main_project.appointment_service.feignclient;


import com.main_project.appointment_service.dto.InvoiceResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "invoice-service", url = "${feign.invoice-service.url}")
public interface InvoiceServiceClient {

    @GetMapping("/invoices/{id}")
    InvoiceResponseDTO getInvoiceById(@PathVariable("id") UUID invoiceId);

}
