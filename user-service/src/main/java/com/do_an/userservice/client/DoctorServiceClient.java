package com.do_an.userservice.client;

import com.do_an.userservice.dto.doctor.DoctorCreateRequest;
import com.do_an.userservice.dto.doctor.DoctorResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "doctor-service")
public interface DoctorServiceClient {

    @PostMapping("/doctor-service/doctors")
    DoctorResponse createDoctor(@RequestBody DoctorCreateRequest request);
}