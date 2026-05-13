package com.do_an.invoiceservice.service;

import com.do_an.common.command.CancelInvoiceCommand;
import com.do_an.invoiceservice.entity.Invoice;
import com.do_an.invoiceservice.entity.InvoiceItem;
import com.do_an.invoiceservice.repository.InvoiceRepository;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceCleanupServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private CommandGateway commandGateway;

    private InvoiceCleanupService invoiceCleanupService;

    private final UUID invoiceId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @BeforeEach
    void setUp() {
        invoiceCleanupService = new InvoiceCleanupService(invoiceRepository, commandGateway);
    }

    @Test
    @DisplayName("INV-CLN-UT-001 - cleanupExpiredInvoices should send cancel command for expired pending invoice")
    void cleanupExpiredInvoicesShouldSendCancelCommandForExpiredPendingInvoice() {
        // Test Case ID: INV-CLN-UT-001 - Objective: overdue pending invoices must be cancelled automatically.
        when(invoiceRepository.findAllByStatusAndIssueAtBefore(eq("PENDING"), any(LocalDateTime.class)))
                .thenReturn(List.of(invoice(invoiceId, "PENDING")));

        invoiceCleanupService.cleanupExpiredInvoices();

        ArgumentCaptor<CancelInvoiceCommand> captor = ArgumentCaptor.forClass(CancelInvoiceCommand.class);
        verify(commandGateway).send(captor.capture());
        assertEquals(invoiceId, captor.getValue().getInvoiceId());
    }

    @Test
    @DisplayName("INV-CLN-UT-002 - cleanupExpiredInvoices should also query waiting invoices")
    void cleanupExpiredInvoicesShouldAlsoQueryWaitingInvoices() {
        // Test Case ID: INV-CLN-UT-002 - Objective: cleanup job should treat WAITING invoices the same as pending ones.
        when(invoiceRepository.findAllByStatusAndIssueAtBefore(eq("PENDING"), any(LocalDateTime.class)))
                .thenReturn(List.of(invoice(invoiceId, "PENDING")));

        invoiceCleanupService.cleanupExpiredInvoices();

        verify(invoiceRepository).findAllByStatusAndIssueAtBefore(eq("WAITING"), any(LocalDateTime.class));
    }

    private Invoice invoice(UUID id, String status) {
        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setStatus(status);
        invoice.setIssueAt(LocalDateTime.now().minusMinutes(31));
        invoice.setItems(new java.util.HashSet<>());
        invoice.addItem(new InvoiceItem());
        return invoice;
    }
}
