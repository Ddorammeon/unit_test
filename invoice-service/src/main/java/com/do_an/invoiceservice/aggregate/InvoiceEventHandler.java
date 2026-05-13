package com.do_an.invoiceservice.aggregate;

import com.do_an.common.event.*;
import com.do_an.common.model.MedicalServiceDTO;
import com.do_an.invoiceservice.dto.request.CreateInvoiceItemRequestDTO;
import com.do_an.invoiceservice.dto.request.CreateInvoiceRequestDTO;
import com.do_an.invoiceservice.dto.response.InvoiceResponseDTO;
import com.do_an.invoiceservice.iservice.IInvoiceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.GenericEventMessage;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static org.axonframework.eventhandling.GenericEventMessage.asEventMessage;


@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceEventHandler {

    private final IInvoiceService invoiceService;
    private final EventBus eventBus;


    @EventHandler
    @Transactional
    public void on(MedicineChargesAddedEvent event) {
        try {
            // Sử dụng service để thêm medicine charges
            InvoiceResponseDTO invoiceResponseDTO = invoiceService.addMedicineCharges(
                    event.getInvoiceId(),
                    event.getInvoiceItemCheckerRequest().getItems(),
                    event.getMedicineItems()
            );

            log.info("Đã cập nhật Invoice DB thành công cho invoice: {}. Tổng tiền mới: {} - Bảo hiểm trả: {} - Bệnh nhân trả: {}",
                    event.getInvoiceId(),
                    invoiceResponseDTO.getTotalAmount(),
                    invoiceResponseDTO.getInsuranceTotalPay(),
                    invoiceResponseDTO.getPatientTotalPay());

        } catch (Exception e) {
            log.error("LỖI NGHIÊM TRỌNG khi cập nhật Invoice DB: {}.", e.getMessage());

            //COMPENSATION: PHÁT SỰ KIỆN LỖI ĐỂ SAGA ROLLBACK
            //Nếu lưu DB thất bại, Saga cần biết để rollback bước Inventory trước đó
            eventBus.publish(asEventMessage(
                    new ChargesAdditionFailedEvent(
                            event.getPrescriptionId(),
                            event.getInvoiceId(),
                            "Lỗi DB Invoice: " + e.getMessage()
                    )
            ));

            throw new RuntimeException("Hoàn tác giao dịch hóa đơn", e);
        }
    }

    @EventHandler
    @Transactional
    public void on(InsuranceDiscountUpdatedEvent event) {
        //BỎ COMMENT NÀY ĐỂ TEST LUỒNG ROLLBACK FULL
        // eventBus.publish(GenericEventMessage.asEventMessage(
        //         new InvoiceDiscountAppliedFailedEvent(
        //                 event.getPrescriptionId(),
        //                 event.getInvoiceId(),
        //                 "Lỗi cơ sở dữ liệu: "
        //         )
        // ));

       try {
           // Sử dụng service để áp dụng insurance discount
           InvoiceResponseDTO invoiceResponseDTO = invoiceService.applyInsuranceDiscount(
                   event.getInvoiceId(),
                   event.getInsuranceClaimId(),
                   event.getDiscountAmount(),
                   event.getItems()
           );

           eventBus.publish(asEventMessage(
                   new InvoiceDiscountAppliedSuccessEvent(
                           event.getPrescriptionId(),
                           event.getInvoiceId()
                   )
           ));

           log.info("Đã cập nhật giảm giá thành công. Bảo hiểm trả: {}, Bệnh nhân trả: {}",
                   invoiceResponseDTO.getInsuranceTotalPay(), invoiceResponseDTO.getPatientTotalPay());
       } catch (Exception e) {
           log.error("LỖI KỸ THUẬT khi cập nhật giảm giá: {}", e.getMessage());
           // COMPENSATION: Nếu lỗi DB, báo Saga biết để Rollback bước trước
           eventBus.publish(asEventMessage(
                   new InvoiceDiscountAppliedFailedEvent(
                           event.getPrescriptionId(),
                           event.getInvoiceId(),
                           "Lỗi cơ sở dữ liệu: " + e.getMessage()
                   )
           ));

           throw new RuntimeException("Hoàn tác Cập nhật Giảm giá", e);
       }
    }

    // --- XỬ LÝ ROLLBACK: HỦY GIẢM GIÁ ---(TẠM THỜI CHƯA DÙNG ĐỂ PHỤC VỤ CHO PAYMENT SAU NÀY)
    @EventHandler
    @Transactional
    public void on(InsuranceDiscountRevertedEvent event) {
        try {
            log.info("Hoàn lại giảm giá bảo hiểm cho hóa đơn: {}", event.getInvoiceId());

            // Sử dụng service để revert insurance discount
            invoiceService.revertInsuranceDiscount(event.getInvoiceId());
            
            log.info("Hoàn lại giảm giá thành công");

        } catch (Exception e) {
            log.error("Không thể hoàn lại giảm giá: {}", e.getMessage());
        }
    }

    // --- XỬ LÝ ROLLBACK: XÓA THUỐC ---
    @EventHandler
    @Transactional
    public void on(MedicineChargesRemovedEvent event) {
        try {
            // Sử dụng service để xóa medicine charges
            invoiceService.removeMedicineCharges(event.getInvoiceId());

            log.info("Phí thuốc đã được xoá thành công cho invoice: {}", event.getInvoiceId());

        } catch (Exception e) {
            log.error("Không thể xoá các phí thuốc: {}", e.getMessage());
        }
    }

    @EventHandler
    @Transactional
    public void on(InvoiceCreateEvent event) {
        try {
            CreateInvoiceRequestDTO requestDTO = buildInvoiceRequest(event);
            InvoiceResponseDTO invoiceResponse = invoiceService.createInvoice(requestDTO);

            log.info("Invoice {} created successfully for appointment {}, publishing InvoicePersistedEvent",
                    invoiceResponse.getId(), event.getAppointmentId());
            eventBus.publish(asEventMessage(new InvoicePersistedEvent(
                    event.getClinicalId(),
                    event.getAppointmentId(),
                    event.getPatientId(),
                    event.getMedicalHistoryId(),
                    invoiceResponse.getId()
            )));

        } catch (Exception e) {
            log.error("Không thể tạo hóa đơn: {}", e.getMessage(), e);
            eventBus.publish(asEventMessage(new InvoicePersistenceFailedEvent(
                    event.getClinicalId(),
                    event.getAppointmentId(),
                    event.getPatientId(),
                    event.getMedicalHistoryId(),
                    event.getInvoiceId(),
                    e.getMessage()
            )));
        }
    }

    private CreateInvoiceRequestDTO buildInvoiceRequest(InvoiceCreateEvent command) {
        CreateInvoiceRequestDTO dto = new CreateInvoiceRequestDTO();
        dto.setId(command.getInvoiceId());
        dto.setAppointmentId(command.getAppointmentId());
        dto.setReceptionistId(command.getDoctorId());
        dto.setCurrency("VND");
        dto.setInsuranceTotalPay(0);
        dto.setPatientTotalPay(0);
        dto.setItems(command.getMedicalServices().stream()
                .map(this::toInvoiceItem)
                .toList());
        log.debug("Invoice payload built with {} items for appointment {}", dto.getItems().size(), command.getAppointmentId());
        return dto;
    }

    private CreateInvoiceItemRequestDTO toInvoiceItem(MedicalServiceDTO serviceDTO) {
        CreateInvoiceItemRequestDTO item = new CreateInvoiceItemRequestDTO();
        item.setId(UUID.randomUUID());
        item.setReferenceId(serviceDTO.getId());
        item.setServiceType(serviceDTO.getServiceType() != null ? serviceDTO.getServiceType() : "MEDICAL_SERVICE");
        item.setQuantity(1);
        item.setDescription(serviceDTO.getServiceName());
        int unitPrice = serviceDTO.getPrice() == null ? 0 : Math.round(serviceDTO.getPrice());
        item.setUnitPrice(unitPrice);
        item.setInsurancePayAmount(0);
        item.setPatientPayAmount(unitPrice);
        return item;
    }
    @EventHandler
    @Transactional
    public void on(InvoiceCancelledEvent event) {
        try {
            // Sử dụng service để cập nhật status
            invoiceService.updateStatus(event.getInvoiceId(), "CANCELLED");
            
            log.info("Đã cập nhật Invoice DB {} thành công sang trạng thái CANCELLED.", event.getInvoiceId());
        } catch (Exception e) {
            log.error("Không tìm thấy Invoice {} trong DB để hủy.", event.getInvoiceId());
        }
    }

    @EventHandler
    @Transactional
    public void on(InvoicePaidEvent event) {
        try {
            log.info("Nhận sự kiện InvoicePaidEvent. Cập nhật trạng thái PAID cho Invoice: {}", event.getInvoiceId());

            // Sử dụng service để cập nhật status
            InvoiceResponseDTO invoiceResponse = invoiceService.updateStatus(event.getInvoiceId(), "PAID");

            // ✅ PHÁT EVENT ĐỂ NOTIFICATION-SERVICE BIẾT
            UUID appointmentId = invoiceResponse.getAppointmentId() != null ?
                    UUID.fromString(invoiceResponse.getAppointmentId()) : null;
            eventBus.publish(asEventMessage(new InvoicePaidNotificationEvent(
                    event.getInvoiceId(),
                    appointmentId,
                    "Hóa đơn " + event.getInvoiceId() + " đã được thanh toán thành công"
            )));

            log.info("Đã cập nhật Invoice {} thành công (PAID).", event.getInvoiceId());

        } catch (Exception e) {
            log.error("Lỗi khi xử lý InvoicePaidEvent cho Invoice {}: {}",
                    event.getInvoiceId(), e.getMessage(), e);
        }
    }




}
