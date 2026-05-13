package com.do_an.invoiceservice.client;


import com.do_an.invoiceservice.dto.response.AppointmentDTO;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "appointment-service",
        url = "${appointment.service.url:http://localhost:8082}"
)
public interface AppointmentClient {

    @GetMapping("/appointment-service/appointments/patient/{patientId}")
    List<AppointmentDTO> getAppointmentsByPatientId(@PathVariable UUID patientId);
}
