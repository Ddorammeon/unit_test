package com.do_an.prescriptionbillingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "inventory-service",
        url = "${inventory.service.url:http://localhost:8084}")
public interface InventoryClient {

    @GetMapping("/inventory-service/dispense-orders/medical-history/{medicalHistoryId}/prescription-status")
    Map<String, Object> getPrescriptionStatusByMedicalHistoryId(@PathVariable("medicalHistoryId") UUID medicalHistoryId);
}


