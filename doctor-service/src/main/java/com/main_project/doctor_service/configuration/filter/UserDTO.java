package com.main_project.doctor_service.configuration.filter;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
public class UserDTO {
    private UUID id;
    private String email;
    private String fullname;
    private String phone;
    private boolean isActive;
    private String imageUrl;
    private LocalDateTime createAt;

    private Set<String> roles;

    private String primaryRole;
}
