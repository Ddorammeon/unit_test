package com.main_project.patient_service.aggregate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalHistoryCreatedEvent {
    private UUID clinicalId;
    private UUID appointmentId;
    private UUID patientId;
    private UUID medicalHistoryId;
}



