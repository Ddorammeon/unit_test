package com.main_project.insurance_service.aggregate;

import com.do_an.common.command.CancelInsuranceClaimCommand;
import com.do_an.common.command.ValidateInsuranceCommand;
import com.do_an.common.event.InsuranceRejectedEvent;
import com.do_an.common.model.InvoiceItemResponse;
import com.main_project.insurance_service.service.IPatientInsuranceService;
import com.main_project.insurance_service.service.IInsuranceClaimService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.GenericEventMessage;
import org.axonframework.modelling.command.Repository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class InsuranceCommandHandler {

    private final IPatientInsuranceService patientInsuranceService;
    private final IInsuranceClaimService insuranceClaimService;
    private final Repository<InsuranceAggregate> insuranceAggregateRepository;
    private final EventBus eventBus;


    @CommandHandler
    @Transactional(readOnly = true)
    public void handle(ValidateInsuranceCommand command) {
        try {
            UUID patientId = command.getPatientId();

            // Sử dụng service để kiểm tra bảo hiểm
            if (!patientInsuranceService.getActiveInsuranceByPatientId(patientId).isPresent()) {
                eventBus.publish(GenericEventMessage.asEventMessage(
                        new InsuranceRejectedEvent(
                                command.getPatientId(),
                                command.getPrescriptionId(),
                                "Không tìm thấy bảo hiểm đang hoạt động cho bệnh nhân: " + patientId
                        )
                ));
                return;
            }

            // Sử dụng service để validate bảo hiểm
            if (!patientInsuranceService.isValidInsurance(patientId)) {
                eventBus.publish(GenericEventMessage.asEventMessage(
                        new InsuranceRejectedEvent(
                                command.getPatientId(),
                                command.getPrescriptionId(),
                                "Bảo hiểm đã hết hạn hoặc không còn hiệu lực"
                        )
                ));
                return;
            }

            // Sử dụng service để xử lý invoice items
            Set<InvoiceItemResponse> itemResponses = insuranceClaimService
                    .processInvoiceItemsForValidation(command.getInvoiceCheckerRequest(), patientId);

            // Tính tổng tiền bảo hiểm chi trả
            Integer totalInsurancePay = itemResponses.stream()
                    .mapToInt(InvoiceItemResponse::getInsurancePayAmount)
                    .sum();

            // Khởi tạo Aggregate để phát sự kiện InsuranceValidatedEvent
            insuranceAggregateRepository.newInstance(() ->
                    new InsuranceAggregate(
                            command.getInsuranceClaimId(),
                            command.getPrescriptionId(),
                            command.getPatientId(),
                            totalInsurancePay,
                            itemResponses
                    )
            );

            log.info("Đã xác thực bảo hiểm {}. Mức bảo hiểm: {}", command.getInsuranceClaimId(), totalInsurancePay);

        } catch (Exception e) {
            log.error("Xác thực bảo hiểm thất bại: {}", e.getMessage());
            eventBus.publish(GenericEventMessage.asEventMessage(
                    new InsuranceRejectedEvent(
                            command.getPatientId(),
                            command.getPrescriptionId(),
                            "Xác thực bảo hiểm thất bại: " + e.getMessage()
                    )
            ));
            throw new RuntimeException("Hoàn tác giao dịch bảo hiểm", e);
        }
    }

    @CommandHandler
    public void handle(CancelInsuranceClaimCommand command) {
        log.info("Nhận lệnh CancelInsuranceClaimCommand cho ClaimId: {}", command.getInsuranceClaimId());

        insuranceAggregateRepository.load(command.getInsuranceClaimId().toString())
                .execute(aggregate -> aggregate.applyCancelInsuranceClaim(
                        command.getInsuranceClaimId(),
                        command.getPrescriptionId(),
                        command.getReason()
                ));
    }
}
