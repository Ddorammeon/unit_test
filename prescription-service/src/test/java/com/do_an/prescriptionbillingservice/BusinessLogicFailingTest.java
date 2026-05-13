/*
 * =============================================================================
 * FILE: BusinessLogicFailingTest.java
 * MODULE UNDER TEST: PrescriptionService.java, PrescriptionBillingController.java,
 *                    PrescriptionCommandHandler.java
 * DESCRIPTION: Opt-in business expectation tests that intentionally fail against
 *              the current implementation to expose missing domain validation.
 * FRAMEWORK: JUnit 5
 * LIBRARIES: Mockito, Spring Boot Test
 * AUTHOR: Student Team
 * DATE: 2026-05-12
 * =============================================================================
 */

package com.do_an.prescriptionbillingservice;

import com.do_an.common.command.CreatePrescriptionCommand;
import com.do_an.common.model.MedicineItem;
import com.do_an.prescriptionbillingservice.aggregate.PrescriptionAggregate;
import com.do_an.prescriptionbillingservice.aggregate.PrescriptionCommandHandler;
import com.do_an.prescriptionbillingservice.client.InventoryClient;
import com.do_an.prescriptionbillingservice.client.InvoiceClient;
import com.do_an.prescriptionbillingservice.controller.PrescriptionBillingController;
import com.do_an.prescriptionbillingservice.dto.CreatePrescriptionRequest;
import com.do_an.prescriptionbillingservice.service.PrescriptionService;
import com.do_an.prescriptionbillingservice.util.InvoiceItemMapper;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.command.Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@EnabledIfSystemProperty(named = "includeBusinessLogicFailures", matches = "true")
class BusinessLogicFailingTest {

    @Mock
    private InventoryClient mock_inventoryClient;

    @Mock
    private CommandGateway mock_commandGateway;

    @Mock
    private Repository<PrescriptionAggregate> mock_prescriptionAggregateRepository;

    @Mock
    private InvoiceClient mock_invoiceClient;

    @Mock
    private InvoiceItemMapper mock_invoiceItemMapper;

    @InjectMocks
    private PrescriptionService prescriptionService;

    @InjectMocks
    private PrescriptionBillingController prescriptionBillingController;

    @InjectMocks
    private PrescriptionCommandHandler prescriptionCommandHandler;

    // TC_BusinessLogic_PrescriptionService_NullMedicalHistoryId_001
    @Test
    void test_getPrescriptionStatus_nullMedicalHistoryId_shouldRejectBeforeCallingInventory() {
        /*
         * Test Case ID : TC_BusinessLogic_PrescriptionService_NullMedicalHistoryId_001
         * Objective    : Verify the desired business rule that medicalHistoryId is required.
         * Input        : medicalHistoryId=null.
         * Expected     : IllegalArgumentException and InventoryClient is not called.
         */
        // --- Arrange: set up invalid null medical history id ---
        UUID uuid_nullMedicalHistoryId = null;

        // --- Act & Assert: expect domain validation before external dependency call ---
        assertThrows(
                IllegalArgumentException.class,
                () -> prescriptionService.getPrescriptionStatus(uuid_nullMedicalHistoryId)
        );
        verify(mock_inventoryClient, never()).getPrescriptionStatusByMedicalHistoryId(any());

        // --- CheckDB: not applicable because this validation should happen before persistence ---
        // --- Rollback: not applicable because no DB state is modified ---
    }

    // TC_BusinessLogic_Controller_EmptyMedicineItems_001
    @Test
    void test_createPrescription_emptyMedicineItems_shouldReturnBadRequestAndNotDispatchCommand() {
        /*
         * Test Case ID : TC_BusinessLogic_Controller_EmptyMedicineItems_001
         * Objective    : Verify the desired business rule that prescriptions require at least one medicine item.
         * Input        : request.items=[] with otherwise valid ids.
         * Expected     : HTTP 400 and CommandGateway is not called.
         */
        // --- Arrange: set up request that violates the medicine item business rule ---
        CreatePrescriptionRequest request_emptyItems = new CreatePrescriptionRequest();
        request_emptyItems.setAppointmentId(UUID.fromString("78787878-7878-7878-7878-787878787878"));
        request_emptyItems.setPatientId(UUID.fromString("89898989-8989-8989-8989-898989898989"));
        request_emptyItems.setDoctorId(UUID.fromString("90909090-aaaa-9090-aaaa-909090909090"));
        request_emptyItems.setMedicalHistoryId(UUID.fromString("91919191-bbbb-9191-bbbb-919191919191"));
        request_emptyItems.setItems(List.of());

        // --- Act: call the controller method under the desired business expectation ---
        ResponseEntity<?> actual_response = prescriptionBillingController.createPrescription(request_emptyItems);

        // --- Assert: expect request rejection instead of command dispatch ---
        assertEquals(HttpStatus.BAD_REQUEST, actual_response.getStatusCode());
        verify(mock_commandGateway, never()).send(any(CreatePrescriptionCommand.class));

        // --- CheckDB: not applicable because invalid request should be rejected before persistence ---
        // --- Rollback: not applicable because no DB state should be modified ---
    }

    // TC_BusinessLogic_CommandHandler_NoInvoice_001
    @Test
    void test_handle_emptyInvoiceList_shouldThrowBusinessExceptionMessage() throws Exception {
        /*
         * Test Case ID : TC_BusinessLogic_CommandHandler_NoInvoice_001
         * Objective    : Verify the desired business rule that an appointment must have an invoice before prescription creation.
         * Input        : Valid command; InvoiceClient returns [].
         * Expected     : IllegalStateException with domain message and no aggregate creation.
         */
        // --- Arrange: set up valid prescription command but no invoice exists ---
        UUID uuid_appointmentId = UUID.fromString("31313131-3131-3131-3131-313131313131");
        CreatePrescriptionCommand command_validPrescription = new CreatePrescriptionCommand(
                UUID.fromString("32323232-3232-3232-3232-323232323232"),
                uuid_appointmentId,
                UUID.fromString("33333333-3333-3333-3333-333333333334"),
                UUID.fromString("34343434-3434-3434-3434-343434343435"),
                UUID.fromString("35353535-3535-3535-3535-353535353536"),
                List.of(new MedicineItem(
                        UUID.fromString("36363636-3636-3636-3636-363636363637"),
                        UUID.fromString("37373737-3737-3737-3737-373737373738"),
                        "Loratadine",
                        1,
                        9000,
                        "tablet",
                        "10mg",
                        "once daily",
                        "3 days",
                        "after dinner"
                ))
        );
        when(mock_invoiceClient.getInvoicesByAppointmentId(uuid_appointmentId)).thenReturn(List.of());

        // --- Act & Assert: expect domain exception instead of raw IndexOutOfBoundsException ---
        IllegalStateException actual_exception = assertThrows(
                IllegalStateException.class,
                () -> prescriptionCommandHandler.handle(command_validPrescription)
        );
        assertEquals("Không tìm thấy hóa đơn cho lịch hẹn", actual_exception.getMessage());
        verify(mock_prescriptionAggregateRepository, never()).newInstance(any());

        // --- CheckDB: not applicable because aggregate creation should not happen ---
        // --- Rollback: not applicable because no repository write should be attempted ---
    }
}
