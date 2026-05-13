package com.do_an.paymentservice.aggregate;

import com.do_an.common.command.CreatePaymentCommand;
import com.do_an.common.command.UpdatePaymentStatusCommand;
import com.do_an.paymentservice.iservice.IPaymentService;
import org.axonframework.modelling.command.Aggregate;
import org.axonframework.modelling.command.Repository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCommandHandlerTest {

    @Mock
    private IPaymentService paymentService;
    @Mock
    private Repository<PaymentAggregate> paymentAggregateRepository;
    @Mock
    private Aggregate<PaymentAggregate> aggregate;
    @Mock
    private PaymentAggregate paymentAggregate;

    @InjectMocks
    private PaymentCommandHandler paymentCommandHandler;

    private final UUID paymentId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID invoiceId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID dispenseOrderId = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Test
    @DisplayName("PAY-SRV-UT-040 - handle(CreatePaymentCommand) should create new payment aggregate instance")
    void handleCreatePaymentCommandShouldCreateNewPaymentAggregateInstance() throws Exception {
        // Test Case ID: PAY-SRV-UT-040 | Objective: Verify CreatePaymentCommand creates a new aggregate instance in the repository.
        paymentCommandHandler.handle(new CreatePaymentCommand(paymentId, invoiceId, dispenseOrderId, 500_000));

        ArgumentCaptor<Callable<PaymentAggregate>> captor = ArgumentCaptor.forClass(Callable.class);
        verify(paymentAggregateRepository).newInstance(captor.capture());
        assertNotNull(captor.getValue());
    }

    @Test
    @DisplayName("PAY-SRV-UT-041 - handle(UpdatePaymentStatusCommand) should load aggregate by payment id string")
    void handleUpdatePaymentStatusCommandShouldLoadAggregateByPaymentIdString() {
        // Test Case ID: PAY-SRV-UT-041 | Objective: Verify UpdatePaymentStatusCommand loads aggregate using paymentId.toString().
        when(paymentAggregateRepository.load(paymentId.toString())).thenReturn(aggregate);

        paymentCommandHandler.handle(new UpdatePaymentStatusCommand(paymentId, "SUCCESSFUL", "paid"));

        verify(paymentAggregateRepository).load(paymentId.toString());
    }

    @Test
    @DisplayName("PAY-SRV-UT-042 - handle(UpdatePaymentStatusCommand) should forward status update to aggregate")
    void handleUpdatePaymentStatusCommandShouldForwardStatusUpdateToAggregate() {
        // Test Case ID: PAY-SRV-UT-042 | Objective: Verify UpdatePaymentStatusCommand executes aggregate status transition with the same payload.
        when(paymentAggregateRepository.load(paymentId.toString())).thenReturn(aggregate);
        doAnswer(invocation -> {
            Consumer<PaymentAggregate> consumer = invocation.getArgument(0);
            consumer.accept(paymentAggregate);
            return null;
        }).when(aggregate).execute(any());

        paymentCommandHandler.handle(new UpdatePaymentStatusCommand(paymentId, "FAILED", "bank rejected"));

        verify(paymentAggregate).applyUpdatePaymentStatus(paymentId, "FAILED", "bank rejected");
    }
}
