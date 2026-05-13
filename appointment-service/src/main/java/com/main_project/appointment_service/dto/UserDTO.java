package com.main_project.appointment_service.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UserDTO {
    private String id;
    private Set<String> roles; // DOCTOR / PATIENT
    private String username;
    private String dob;
}
