package com.do_an.prescriptionbillingservice.controller;

import com.do_an.common.command.CreatePrescriptionCommand;
import com.do_an.prescriptionbillingservice.dto.CreatePrescriptionRequest;
import com.do_an.prescriptionbillingservice.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/prescription-billing-service/prescription-billings")
@RequiredArgsConstructor
public class PrescriptionBillingController {
    private final CommandGateway commandGateway;
    private final PrescriptionService prescriptionService;

    @PostMapping
    public ResponseEntity<?> createPrescription(@RequestBody CreatePrescriptionRequest request) {
        UUID prescriptionId = UUID.randomUUID();

        // Kiểm tra trạng thái đơn đã tồn tại (SOLD/RELEASE) cho hồ sơ
//        if (prescriptionValidationService.hasBlockingPrescription(request.getMedicalHistoryId())) {
//            return ResponseEntity.status(HttpStatus.CONFLICT)
//                    .body(Map.of("message", "Hồ sơ đã có đơn thuốc ở trạng thái không cho phép tạo mới (RELEASE/SOLD)"));
//        }

        commandGateway.send(new CreatePrescriptionCommand(
                        prescriptionId,
                        request.getAppointmentId(),
                        request.getPatientId(),
                        request.getDoctorId(),
                        request.getMedicalHistoryId(),
                        request.getItems()
                ));

        // Trả về ACCEPTED để báo đang xử lý saga
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("id", prescriptionId, "message", "Đang xử lý đơn thuốc"));
    }

    @GetMapping("/medical-history/{medicalHistoryId}/status")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable UUID medicalHistoryId) {
        Map<String, Object> status = prescriptionService.getPrescriptionStatus(medicalHistoryId);
        return ResponseEntity.ok(status);
    }
}
