package com.do_an.prescriptionbillingservice.aggregate;


import com.do_an.common.command.CreatePrescriptionCommand;
import com.do_an.common.model.InvoiceItemCheckerRequest;
import com.do_an.prescriptionbillingservice.client.InvoiceClient;
import com.do_an.prescriptionbillingservice.dto.response.InvoiceResponseDTO;
import com.do_an.prescriptionbillingservice.util.InvoiceItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.modelling.command.Repository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PrescriptionCommandHandler {
    private final Repository<PrescriptionAggregate> prescriptionAggregateRepository;
    private final InvoiceClient invoiceClient;
    private final InvoiceItemMapper invoiceItemMapper;


    @CommandHandler
    public void handle(CreatePrescriptionCommand command) {
        log.info("CreatePrescriptionCommand appointmentId={}, prescriptionId={}, items={}",
                command.getAppointmentId(), command.getPrescriptionId(), command.getItems() != null ? command.getItems().size() : 0);

        if (command.getItems() == null || command.getItems().isEmpty()) {
            throw new IllegalStateException("Không có thuốc để tạo đơn thuốc");
        }

        List<InvoiceResponseDTO> invoices = invoiceClient.getInvoicesByAppointmentId(command.getAppointmentId());

        List<InvoiceItemCheckerRequest> serviceItems =  invoiceItemMapper.toCheckerRequests(invoices.get(0).getItems());

        try{
            prescriptionAggregateRepository.newInstance(() -> new PrescriptionAggregate(
                    command.getPrescriptionId(),
                    invoices.get(0).getId(),
                    command.getPatientId(),
                    command.getDoctorId(),
                    command.getMedicalHistoryId(),
                    command.getItems(),
                    serviceItems

            ));

        }catch (Exception e){
            throw new RuntimeException(e);
        }



    }
}
