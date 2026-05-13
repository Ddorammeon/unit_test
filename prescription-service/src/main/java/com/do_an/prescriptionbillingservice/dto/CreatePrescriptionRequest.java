package com.do_an.prescriptionbillingservice.dto;

import com.do_an.common.model.MedicineItem;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreatePrescriptionRequest {
    // ID của lần khám (MedicalHistory)
    private UUID medicalHistoryId;

    private UUID appointmentId;

    // ID bệnh nhân
    private UUID patientId;

    // ID bác sĩ kê đơn
    private UUID doctorId;

    // Ghi chú chung của đơn thuốc (nếu có)
    private String note;

    // Chuẩn đoán
    private String diagnosis;

    // Danh sách thuốc kê trong đơn
    private List<MedicineItem> items;


}
