package com.do_an.userservice.client;

import com.do_an.userservice.dto.patient.PatientCreateRequest;
import com.do_an.userservice.dto.patient.PatientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "patient-service",
             url = "${patient.service.url:http://localhost:8089}"

)
public interface PatientServiceClient {

    @PostMapping("/patient-service/patients")
    PatientResponse createPatient(@RequestBody PatientCreateRequest request);
}