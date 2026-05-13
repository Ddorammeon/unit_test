package com.main_project.insurance_service.aggregate;

import com.do_an.common.event.InsuranceClaimCancelledEvent;
import com.do_an.common.event.InsuranceRejectedEvent;
import com.do_an.common.event.InsuranceValidatedEvent;
import com.do_an.common.model.InvoiceItemResponse;
import com.main_project.insurance_service.service.IClaimItemService;
import com.main_project.insurance_service.service.IInsuranceClaimService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class InsuranceEventHandler {
    
    private final IClaimItemService claimItemService;
    private final IInsuranceClaimService insuranceClaimService;

    @EventHandler
    @Transactional
    public void on(InsuranceValidatedEvent event) {
        try {
            // Sử dụng service để tạo claim từ event
            insuranceClaimService.createClaimFromValidationEvent(
                    event.getInsuranceClaimId(),
                    event.getPatientId(),
                    event.getItems()
            );

            // Lưu chi tiết Claim Items sử dụng service
            for (InvoiceItemResponse item : event.getItems()) {
                if (item.getInsurancePayAmount() > 0 || item.getClaimItemId() != null) {
                    // Đảm bảo claimItemId không null trước khi lưu
                    if (item.getClaimItemId() == null) {
                        log.warn("ClaimItemId là null đối với mục có insurancePayAmount: {}. Bỏ qua.",
                                item.getInsurancePayAmount());
                        continue;
                    }

                    var request = insuranceClaimService
                            .createClaimItemRequestFromInvoiceItem(item, event.getInsuranceClaimId());
                    claimItemService.createClaimItem(request);
                }
            }

            log.info("Đã lưu thành công InsuranceClaim {} vào DB.", event.getInsuranceClaimId());
        } catch (Exception e) {
            log.error("Lỗi khi lưu DB từ Event: {}", e.getMessage());
            throw e;
        }
    }


    @EventHandler
    @Transactional
    public void on(InsuranceClaimCancelledEvent event) {
        try {
            // Sử dụng service để reject claim
            insuranceClaimService.rejectClaim(event.getInsuranceClaimId(), event.getReason());

            log.info("Đã cập nhật trạng thái REJECTED cho InsuranceClaim {}", event.getInsuranceClaimId());
        } catch (Exception e) {
            log.error("Lỗi khi lưu DB để hủy InsuranceClaim: {}", e.getMessage());
        }
    }

    @EventHandler
    @Transactional
    public void on(InsuranceRejectedEvent event) {
        log.info("Nhận sự kiện InsuranceRejectedEvent. Lý do: {}", event.getReason());
    }

}
