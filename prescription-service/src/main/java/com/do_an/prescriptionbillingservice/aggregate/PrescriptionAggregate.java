package com.do_an.prescriptionbillingservice.aggregate;

import com.do_an.common.event.PrescriptionCreatedEvent;
import com.do_an.common.model.InvoiceItemCheckerRequest;
import com.do_an.common.model.MedicineItem;
import lombok.NoArgsConstructor;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.List;
import java.util.UUID;

@Aggregate
@NoArgsConstructor
public class PrescriptionAggregate {
    @AggregateIdentifier
    private UUID prescriptionId;
    private String status;

    public PrescriptionAggregate(UUID prescriptionId, UUID invoiceId, UUID patientId, UUID doctorId, UUID medicalHistoryId, List<MedicineItem> items,
                                 List<InvoiceItemCheckerRequest> serviceItems) {
        //Kích hoạt Saga
        AggregateLifecycle.apply(new PrescriptionCreatedEvent(
                prescriptionId,
                invoiceId,
                patientId,
                doctorId,
                medicalHistoryId,
                items,
                serviceItems
        ));


    }

    @EventSourcingHandler
    public void on(PrescriptionCreatedEvent event) {
        this.prescriptionId = event.getPrescriptionId();
        this.status = "CREATED";
    }

}
