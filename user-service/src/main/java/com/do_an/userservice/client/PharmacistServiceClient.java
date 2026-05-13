package com.do_an.userservice.client;

import com.do_an.userservice.dto.pharmacist.PharmacistCreateRequest;
import com.do_an.userservice.dto.pharmacist.PharmacistResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service")
public interface PharmacistServiceClient {

    @PostMapping("/inventory-service/pharmacists")
    PharmacistResponse createPharmacist(@RequestBody PharmacistCreateRequest request);
}