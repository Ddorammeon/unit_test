package com.do_an.invoiceservice.client;


import com.do_an.invoiceservice.dto.response.DispenseOrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "inventory-service",
        url = "${inventory.service.url:http://localhost:8084}"
)

public interface InventoryClient {

    @PatchMapping("/inventory-service/dispense-orders/{id}/sold")
    DispenseOrderResponse markAsSold(@PathVariable("id") UUID dispenseOrderId);
}
