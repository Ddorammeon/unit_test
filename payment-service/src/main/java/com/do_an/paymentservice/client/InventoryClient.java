package com.do_an.paymentservice.client;

import com.do_an.paymentservice.dto.response.DispenseOrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "inventory-service",
        url = "${inventory.service.url:http://localhost:8084}"
)
public interface InventoryClient {

//    @PatchMapping("/inventory-service/dispense-orders/{id}/sold")
//    DispenseOrderResponse (@PathVariable("id") UUID dispenseOrderId);

    @GetMapping("/inventory-service/dispense-orders/prescription/{id}")
    DispenseOrderResponse getByPrescriptionId(@PathVariable UUID id);

    @GetMapping("/inventory-service/dispense-orders/medical-history/{id}")
    ResponseEntity<DispenseOrderResponse> getByMedicalHistoryId(@PathVariable UUID id);

}
