package com.main_project.appointment_service.feignclient;

import com.main_project.appointment_service.dto.DoctorWorkScheduleDTO;
import com.main_project.appointment_service.dto.WorkScheduleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "doctor-service", url = "${feign.doctor-service.url}")
public interface DoctorServiceClient {

    @GetMapping("/doctor-work-schedules/doctor/{doctorId}")
    List<DoctorWorkScheduleDTO> getDoctorWorkSchedules(@PathVariable("doctorId") UUID doctorId);

    @GetMapping("/work-schedules/{workScheduleId}")
    WorkScheduleDTO getWorkSchedule(@PathVariable("workScheduleId") UUID workScheduleId);
}
