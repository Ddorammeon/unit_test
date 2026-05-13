package com.main_project.appointment_service.dto;

import com.main_project.appointment_service.enums.MedicalServiceStatus;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MedicalServiceDTO {
    private UUID id;
    private String serviceName;
    private String serviceType;
    private Integer serviceTime;
    private MedicalServiceStatus status;
    private Float price;
    private String description;
    private String imgUrl;
}
