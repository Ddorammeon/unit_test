package com.do_an.invoiceservice.aggregate;


import com.do_an.common.command.RemoveMedicineChargesCommand;
import com.do_an.common.command.RevertInsuranceDiscountCommand;
import com.do_an.common.event.*;
import com.do_an.common.model.InvoiceCheckerRequest;
import com.do_an.common.model.InvoiceItemResponse;
import com.do_an.common.model.MedicalServiceDTO;
import com.do_an.common.model.MedicineItem;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.util.List;
import java.util.Set;
import java.util.UUID;


@Aggregate
@NoArgsConstructor
public class InvoiceAggregate {

   @AggregateIdentifier
   private UUID invoiceId;

    // Trạng thái nội tại (nếu cần cho validation sau này, ví dụ để chặn lệnh duplicate)

    private UUID prescriptionId;

    private UUID insuranceClaimId;

    private boolean medicineChargesAdded;
    private boolean insuranceApplied;


    private String status;

    public InvoiceAggregate(UUID clinicalId, UUID invoiceId, UUID appointmentId, UUID patientId, UUID medicalHistoryId, UUID doctorId, List<MedicalServiceDTO> medicalServices) {
        AggregateLifecycle.apply(new InvoiceCreateEvent(
                clinicalId,
                invoiceId,
                appointmentId,
                patientId,
                medicalHistoryId,
                doctorId,
                medicalServices
        ));
    }

    public InvoiceAggregate(UUID prescriptionId,
                            List<MedicineItem> medicineItems,
                            InvoiceCheckerRequest invoiceCheckerRequest) {
        AggregateLifecycle.apply(new MedicineChargesAddedEvent(
                prescriptionId,
                invoiceCheckerRequest.getId(),
                medicineItems,
                invoiceCheckerRequest
        ));
    }

    public void applyAddMedicineCharges(UUID prescriptionId,
                                   List<MedicineItem> medicineItems,
                                   InvoiceCheckerRequest invoiceCheckerRequest) {
        AggregateLifecycle.apply(new MedicineChargesAddedEvent(
                prescriptionId,
                invoiceCheckerRequest.getId(),
                medicineItems,
                invoiceCheckerRequest
        ));
    }

    public void applyInsuranceDiscount(UUID insuranceClaimId, UUID prescriptionId, UUID invoiceId, Integer discountAmount, Set<InvoiceItemResponse> items) {
        AggregateLifecycle.apply(new InsuranceDiscountUpdatedEvent(
                insuranceClaimId,
                prescriptionId,
                invoiceId,
                discountAmount,
                items
        ));
    }

    public void applyInvoicePaid(UUID invoiceId){
        AggregateLifecycle.apply(new InvoicePaidEvent(invoiceId));

    }

    public void applyCancelInvoice(UUID invoiceId, String reason){
        UUID insuranceClaimId;

        if(this.insuranceApplied == false){
            insuranceClaimId = null;
        }
        else{
            insuranceClaimId = this.insuranceClaimId;
        }


        AggregateLifecycle.apply(new InvoiceCancelledEvent(
                invoiceId,
                this.prescriptionId,
                insuranceClaimId,
                reason
        ));
    }


    public void applyRevertInsuranceDiscount(UUID prescriptionId, UUID invoiceId){
        AggregateLifecycle.apply(new InsuranceDiscountRevertedEvent(
                prescriptionId,
                invoiceId
        ));
    }

//   @CommandHandler
//   public void handle(RevertInsuranceDiscountCommand command) {
//       AggregateLifecycle.apply(new InsuranceDiscountRevertedEvent(
//               command.getPrescriptionId(),
//               command.getInvoiceId()
//       ));
//   }

    public void applyRemoveMedicineCharges(UUID prescriptionId, UUID invoiceId){
        AggregateLifecycle.apply(new MedicineChargesRemovedEvent(
                prescriptionId,
                invoiceId
        ));
    }

//   @CommandHandler
//   public void handle(RemoveMedicineChargesCommand command) {
//       AggregateLifecycle.apply(new MedicineChargesRemovedEvent(
//               command.getPrescriptionId(),
//               command.getInvoiceId()
//       ));
//   }

   @EventSourcingHandler
   public void on(MedicineChargesAddedEvent event) {
       this.invoiceId = event.getInvoiceId();
       this.prescriptionId = event.getPrescriptionId();
       this.medicineChargesAdded = true;
       this.status = "PENDING";
   }

    @EventSourcingHandler
    public void on(InvoiceCreateEvent event) {
        this.invoiceId = event.getInvoiceId();
    }

   @EventSourcingHandler
   public void on(InsuranceDiscountUpdatedEvent event) {
        this.insuranceClaimId = event.getInsuranceClaimId();
        this.insuranceApplied = true;
   }

   @EventSourcingHandler
   public void on(InsuranceDiscountRevertedEvent event) {
        this.insuranceApplied = false;
   }

   @EventSourcingHandler
   public void on(MedicineChargesRemovedEvent event) {

        this.medicineChargesAdded = false;
   }

   @EventSourcingHandler
   public void on(InvoicePaidEvent event){
       this.status = "PAID";
   }
}

