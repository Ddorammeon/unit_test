package com.do_an.prescriptionbillingservice.service;

import com.do_an.prescriptionbillingservice.client.InventoryClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final InventoryClient inventoryClient;

//    public boolean hasBlockingPrescription(UUID medicalHistoryId) {
//        Map<String, Object> status = inventoryClient.getPrescriptionStatusByMedicalHistoryId(medicalHistoryId);
//        String value = (String) status.getOrDefault("status", "NONE");
//        return "SOLD".equalsIgnoreCase(value) || "RELEASED".equalsIgnoreCase(value);
//    }

    public Map<String, Object> getPrescriptionStatus(UUID medicalHistoryId) {
        return inventoryClient.getPrescriptionStatusByMedicalHistoryId(medicalHistoryId);
    }
}


