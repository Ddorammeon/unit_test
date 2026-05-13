package com.main_project.notification_service.client;

import com.main_project.notification_service.dto.response.PharmacistResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "inventory-service",
            url = "${inventory.service.url:http://localhost:8084}")
public interface InventoryClient {

    @GetMapping("/inventory-service/pharmacists")
    List<PharmacistResponse> getAllPharmacists();
}
