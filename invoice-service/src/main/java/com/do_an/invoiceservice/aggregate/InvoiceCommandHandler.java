package com.do_an.invoiceservice.aggregate;


import com.do_an.common.command.*;

import com.do_an.common.model.InvoiceCheckerRequest;
import com.do_an.common.model.InvoiceItemCheckerRequest;
import com.do_an.invoiceservice.exception.InvoiceNotFoundException;
import com.do_an.invoiceservice.iservice.IInvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.axonframework.modelling.command.Repository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceCommandHandler {
    
    private final IInvoiceService invoiceService;
    private final Repository<InvoiceAggregate> invoiceAggregateRepository;
    @CommandHandler
    @Transactional(readOnly = true)
    public void handle(AddMedicineChargesCommand command) throws Exception {
        Set<InvoiceItemCheckerRequest> itemCheckers = command.getMedicineItems().stream()
                .map(item -> {
                    InvoiceItemCheckerRequest itemReq = new InvoiceItemCheckerRequest();
                    itemReq.setId(UUID.randomUUID());
                    itemReq.setReferenceId(item.getMedicineId());
                    itemReq.setServiceType("MEDICINE");
                    itemReq.setQuantity(item.getQuantity());
                    itemReq.setUnitPrice(item.getUnitPrice());
                    itemReq.setDescription(item.getName());
                    return itemReq;
                })
                .collect(Collectors.toSet());

        InvoiceCheckerRequest invoiceCheckerRequest = new InvoiceCheckerRequest();
        invoiceCheckerRequest.setId(command.getInvoiceId());
        invoiceCheckerRequest.setItems(itemCheckers);

        // Sử dụng service để validate
        if (!invoiceService.canAddMedicineCharges(command.getInvoiceId())) {
            throw new IllegalStateException("Hoá đơn ở trạng thái khác PENDING, Không thể thêm thuốc !");
        }

        try {
            invoiceAggregateRepository.load(command.getInvoiceId().toString())
                    .execute(aggregate -> aggregate.applyAddMedicineCharges(
                            command.getPrescriptionId(),
                            command.getMedicineItems(),
                            invoiceCheckerRequest
                    ));
        } catch (AggregateNotFoundException e) {
            invoiceAggregateRepository.newInstance(() -> new InvoiceAggregate(
                    command.getPrescriptionId(),
                    command.getMedicineItems(),
                    invoiceCheckerRequest
            ));
        }
    }

    @CommandHandler
    @Transactional(readOnly = true)
    public void handle(ApplyInsuranceDiscountCommand command) {
        log.info("Xử lý ApplyInsuranceDiscountCommand cho InvoiceId: {}", command.getInvoiceId());

        // Sử dụng service để validate
        if (!invoiceService.canApplyInsuranceDiscount(command.getInvoiceId())) {
            throw new IllegalStateException("Hoá đơn ở trạng thái khác PENDING, Không thể áp dụng bảo hiểm !");
        }

        if (command.getDiscountAmount() < 0) {
            throw new IllegalArgumentException("Số tiền giảm giá không được âm");
        }

        invoiceAggregateRepository.load(command.getInvoiceId().toString())
                .execute(aggregate -> aggregate.applyInsuranceDiscount(
                        command.getInsuranceClaimId(),
                        command.getPrescriptionId(),
                        command.getInvoiceId(),
                        command.getDiscountAmount(),
                        command.getItems()
                ));
    }

    @CommandHandler
    @Transactional
    public void handle(CreateInvoiceCommand command) throws Exception {
        log.info("CreateInvoiceCommand clinicalId={}, appointmentId={}, services={}",
                command.getClinicalId(), command.getAppointmentId(), command.getMedicalServices() != null ? command.getMedicalServices().size() : 0);

        if (command.getMedicalServices() == null || command.getMedicalServices().isEmpty()) {
            throw new IllegalStateException("Không có dịch vụ y tế để tạo hóa đơn");
        }

        invoiceAggregateRepository.newInstance(() -> new InvoiceAggregate(
                command.getClinicalId(),
                command.getInvoiceId(),
                command.getAppointmentId(),
                command.getPatientId(),
                command.getMedicalHistoryId(),
                command.getDoctorId(),
                command.getMedicalServices()
        ));
    }

    @CommandHandler
    @Transactional(readOnly = true)
    public void handle(CancelInvoiceCommand command) {
        // Sử dụng service để validate
        if (!invoiceService.canCancel(command.getInvoiceId())) {
            throw new IllegalStateException("Hoá đơn đã thành toán, không thể hủy hóa đơn !");
        }

        invoiceAggregateRepository.load(command.getInvoiceId().toString())
                .execute(aggregate -> aggregate.applyCancelInvoice(
                        command.getInvoiceId(),
                        command.getReason()
                ));
    }

    @CommandHandler
    public void handle(MarkInvoiceAsPaidCommand command) {
        // Sử dụng service để validate
        if (!invoiceService.canMarkAsPaid(command.getInvoiceId())) {
            // Check if already paid (idempotent)
            try {
                var invoice = invoiceService.getInvoiceById(command.getInvoiceId());
                if ("PAID".equals(invoice.getStatus())) {
                    return; // Already paid, idempotent
                }
            } catch (InvoiceNotFoundException e) {
                throw new RuntimeException("Invoice không tồn tại", e);
            }
            throw new IllegalStateException("Không thể thanh toán hóa đơn này");
        }

        invoiceAggregateRepository.load(command.getInvoiceId().toString())
                .execute(aggregate -> aggregate.applyInvoicePaid(
                        command.getInvoiceId()
                ));
    }

    @CommandHandler
    public void handle(RevertInsuranceDiscountCommand command) {
        log.info("Hoàn lại giảm giá bảo hiểm cho hóa đơn: {}", command.getInvoiceId());
        
        // Sử dụng service để validate invoice exists
        invoiceService.getInvoiceById(command.getInvoiceId());

        invoiceAggregateRepository.load(command.getInvoiceId().toString())
                .execute(aggregate -> aggregate.applyRevertInsuranceDiscount(
                        command.getPrescriptionId(),
                        command.getInvoiceId()
                ));
    }

    @CommandHandler
    public void handle(RemoveMedicineChargesCommand command) {
        log.info("Loại bỏ phí thuốc cho hóa đơn: {}", command.getInvoiceId());

        // Sử dụng service để validate invoice exists
        invoiceService.getInvoiceById(command.getInvoiceId());

        invoiceAggregateRepository.load(command.getInvoiceId().toString())
                .execute(aggregate -> aggregate.applyRemoveMedicineCharges(
                        command.getPrescriptionId(),
                        command.getInvoiceId()
                ));
    }
}