package com.do_an.userservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.UUID;

@Data
public class RoleDTO {
    private UUID id;
    private String roleName;
}