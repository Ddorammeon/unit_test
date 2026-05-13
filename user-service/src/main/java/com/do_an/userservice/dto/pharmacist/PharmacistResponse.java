package com.do_an.userservice.dto.pharmacist;

import lombok.Data;

import java.util.UUID;

@Data
public class PharmacistResponse {
    private UUID userId;
    private String degree;
    private String certificate;
}