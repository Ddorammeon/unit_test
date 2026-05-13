package com.main_project.insurance_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDocumentRequestDTO {
    
    @NotBlank(message = "File path is required")
    private String filePath;
    
    @NotBlank(message = "Document type is required")
    private String documentType;
    
    @NotBlank(message = "Status is required")
    private String status;
    
    @NotNull(message = "Upload time is required")
    private LocalDateTime uploadAt;
    
    @NotNull(message = "Insurance claim ID is required")
    private UUID insuranceClaimId;
}





