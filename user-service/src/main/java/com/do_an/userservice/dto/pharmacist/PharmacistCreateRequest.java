package com.do_an.userservice.dto.pharmacist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacistCreateRequest {
    private UUID userId;
    private String degree;
    private String certificate;
}