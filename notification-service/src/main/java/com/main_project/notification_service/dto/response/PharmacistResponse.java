package com.main_project.notification_service.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class PharmacistResponse {
    private UUID userId;
    private String degree;
    private String certificate;
}
