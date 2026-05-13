package com.main_project.appointment_service.feignclient;

import com.main_project.appointment_service.dto.DoctorDTO;
import com.main_project.appointment_service.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "user-service", url = "${feign.user-service.url:http://localhost:8085/user-service/users}")
public interface UserServiceClient {
    @GetMapping("{userId}")
    UserDTO getUserById(@PathVariable("userId") UUID userId);
}
