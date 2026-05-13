package com.main_project.insurance_service.aggregate;


import com.do_an.common.event.InsuranceClaimCancelledEvent;
import com.do_an.common.event.InsuranceRejectedEvent;
import com.do_an.common.event.InsuranceValidatedEvent;
import com.do_an.common.model.InvoiceItemResponse;
import lombok.NoArgsConstructor;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import java.util.*;

@Aggregate
@NoArgsConstructor
public class InsuranceAggregate {

    @AggregateIdentifier
    private UUID insuranceClaimId;
    
    private String status;

    public InsuranceAggregate(UUID insuranceClaimId, UUID prescriptionId, UUID patientId, Integer coverageAmount, Set<InvoiceItemResponse> items) {
        AggregateLifecycle.apply(new InsuranceValidatedEvent(
                insuranceClaimId,
                prescriptionId,
                patientId,
                coverageAmount,
                items
        ));

    }

    public void applyCancelInsuranceClaim(UUID insuranceClaimId, UUID prescriptionId, String reason){
        AggregateLifecycle.apply(new InsuranceClaimCancelledEvent(
                insuranceClaimId,
                prescriptionId,
                reason
        ));

    }

    @EventSourcingHandler
    public void on(InsuranceValidatedEvent event) {
        this.insuranceClaimId = event.getInsuranceClaimId();
        this.status = "VALIDATED";
    }

    @EventSourcingHandler
    public void on(InsuranceRejectedEvent event) {
        this.status = "REJECTED";
    }



}

