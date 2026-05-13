package com.do_an.invoiceservice.aggregate;

import com.do_an.common.model.MedicalServiceDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceCreateEvent {
    private UUID clinicalId;
    private UUID invoiceId;
    private UUID appointmentId;
    private UUID patientId;
    private UUID medicalHistoryId;
    private UUID doctorId;
    private List<MedicalServiceDTO> medicalServices;
}
