package com.do_an.paymentservice.client;


import com.do_an.paymentservice.dto.response.MedicalHistoryResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "patient-service",
        url = "${patient.service.url:http://localhost:8089}"
)
public interface PatientClient {

    @GetMapping("/patient-service/medical-histories/appointment/{appointmentId}")
    List<MedicalHistoryResponseDTO> getByAppointment(@PathVariable UUID appointmentId);

}
