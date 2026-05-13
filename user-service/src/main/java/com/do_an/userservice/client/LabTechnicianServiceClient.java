package com.do_an.userservice.client;

import com.do_an.userservice.dto.labtechnician.LabTechnicianCreateRequest;
import com.do_an.userservice.dto.labtechnician.LabTechnicianResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "labtest-service")
public interface LabTechnicianServiceClient {

    @PostMapping("/labtest-service/lab-technicians")
    LabTechnicianResponse createLabTechnician(@RequestBody LabTechnicianCreateRequest request);
}