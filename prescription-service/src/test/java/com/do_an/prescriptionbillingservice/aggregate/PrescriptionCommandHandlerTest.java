/*
 * =============================================================================
 * FILE: PrescriptionCommandHandlerTest.java
 * MODULE UNDER TEST: PrescriptionCommandHandler.java
 * DESCRIPTION: Unit tests for prescription command handling, including invoice
 *              lookup, invoice item mapping, aggregate creation, and error paths.
 * FRAMEWORK: JUnit 5
 * LIBRARIES: Mockito, Spring Boot Test
 * AUTHOR: Student Team
 * DATE: 2026-05-12
 * =============================================================================
 */

package com.do_an.prescriptionbillingservice.aggregate;

import com.do_an.common.command.CreatePrescriptionCommand;
import com.do_an.common.model.InvoiceItemCheckerRequest;
import com.do_an.common.model.MedicineItem;
import com.do_an.prescriptionbillingservice.client.InvoiceClient;
import com.do_an.prescriptionbillingservice.dto.response.InvoiceItemResponseDTO;
import com.do_an.prescriptionbillingservice.dto.response.InvoiceResponseDTO;
import com.do_an.prescriptionbillingservice.util.InvoiceItemMapper;
import org.axonframework.modelling.command.Repository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrescriptionCommandHandlerTest {

    @Mock
    private Repository<PrescriptionAggregate> mock_prescriptionAggregateRepository;

    @Mock
    private InvoiceClient mock_invoiceClient;

    @Mock
    private InvoiceItemMapper mock_invoiceItemMapper;

    @InjectMocks
    private PrescriptionCommandHandler prescriptionCommandHandler;

    // TC_PrescriptionCommandHandler_Handle_001
    @Test
    void test_handle_validCommand_createsPrescriptionAggregate() throws Exception {
        /*
         * Test Case ID : TC_PrescriptionCommandHandler_Handle_001
         * Objective    : Verify that a valid command loads invoice data, maps service items, and creates aggregate.
         * Input        : CreatePrescriptionCommand with one MedicineItem and one invoice with one InvoiceItemResponseDTO.
         * Expected     : Repository.newInstance is called once after invoice lookup and mapper call.
         */
        // --- Arrange: set up command, invoice response, mapped service items, and captor ---
        UUID uuid_prescriptionId = UUID.fromString("10101010-1010-1010-1010-101010101010");
        UUID uuid_appointmentId = UUID.fromString("20202020-2020-2020-2020-202020202020");
        UUID uuid_patientId = UUID.fromString("30303030-3030-3030-3030-303030303030");
        UUID uuid_doctorId = UUID.fromString("40404040-4040-4040-4040-404040404040");
        UUID uuid_medicalHistoryId = UUID.fromString("50505050-5050-5050-5050-505050505050");
        List<MedicineItem> list_validMedicineItems = List.of(new MedicineItem(
                UUID.fromString("60606060-6060-6060-6060-606060606060"),
                UUID.fromString("70707070-7070-7070-7070-707070707070"),
                "Ibuprofen",
                3,
                8000,
                "tablet",
                "200mg",
                "twice daily",
                "4 days",
                "after meals"
        ));
        CreatePrescriptionCommand command_validPrescription = new CreatePrescriptionCommand(
                uuid_prescriptionId,
                uuid_appointmentId,
                uuid_patientId,
                uuid_doctorId,
                uuid_medicalHistoryId,
                list_validMedicineItems
        );
        InvoiceItemResponseDTO invoiceItemResponseDTO_serviceCharge = new InvoiceItemResponseDTO();
        invoiceItemResponseDTO_serviceCharge.setId(UUID.fromString("80808080-8080-8080-8080-808080808080"));
        invoiceItemResponseDTO_serviceCharge.setDescription("Dental checkup");
        InvoiceResponseDTO invoiceResponseDTO_existingInvoice = new InvoiceResponseDTO();
        invoiceResponseDTO_existingInvoice.setId(UUID.fromString("90909090-9090-9090-9090-909090909090"));
        invoiceResponseDTO_existingInvoice.setItems(List.of(invoiceItemResponseDTO_serviceCharge));
        List<InvoiceItemCheckerRequest> list_mappedServiceItems = List.of(new InvoiceItemCheckerRequest(
                UUID.fromString("81818181-8181-8181-8181-818181818181"),
                UUID.fromString("82828282-8282-8282-8282-828282828282"),
                "SERVICE",
                1,
                "Dental checkup",
                100000
        ));
        when(mock_invoiceClient.getInvoicesByAppointmentId(uuid_appointmentId))
                .thenReturn(List.of(invoiceResponseDTO_existingInvoice));
        when(mock_invoiceItemMapper.toCheckerRequests(invoiceResponseDTO_existingInvoice.getItems()))
                .thenReturn(list_mappedServiceItems);
        ArgumentCaptor<Callable<PrescriptionAggregate>> captor_aggregateFactory = ArgumentCaptor.forClass(Callable.class);

        // --- Act: handle the command under test ---
        prescriptionCommandHandler.handle(command_validPrescription);

        // --- Assert: verify dependency calls and aggregate creation request ---
        verify(mock_invoiceClient).getInvoicesByAppointmentId(uuid_appointmentId);
        verify(mock_invoiceItemMapper).toCheckerRequests(invoiceResponseDTO_existingInvoice.getItems());
        verify(mock_prescriptionAggregateRepository).newInstance(captor_aggregateFactory.capture());

        // --- CheckDB: not applicable because repository is mocked and no DB is accessed in this unit test ---
        // --- Rollback: not applicable because repository write is isolated by Mockito mock ---
    }

    // TC_PrescriptionCommandHandler_Handle_002
    @Test
    void test_handle_emptyMedicineItems_throwsIllegalStateException() throws Exception {
        /*
         * Test Case ID : TC_PrescriptionCommandHandler_Handle_002
         * Objective    : Verify business rule violation when no medicine items are provided.
         * Input        : CreatePrescriptionCommand.items=[].
         * Expected     : Throws IllegalStateException and does not call InvoiceClient or Repository.
         */
        // --- Arrange: set up command with empty medicine item list ---
        CreatePrescriptionCommand command_emptyItems = new CreatePrescriptionCommand(
                UUID.fromString("11111111-aaaa-1111-aaaa-111111111111"),
                UUID.fromString("22222222-bbbb-2222-bbbb-222222222222"),
                UUID.fromString("33333333-cccc-3333-cccc-333333333333"),
                UUID.fromString("44444444-dddd-4444-dddd-444444444444"),
                UUID.fromString("55555555-eeee-5555-eeee-555555555555"),
                List.of()
        );

        // --- Act & Assert: verify the business rule exception ---
        IllegalStateException actual_exception = assertThrows(
                IllegalStateException.class,
                () -> prescriptionCommandHandler.handle(command_emptyItems)
        );
        assertEquals("Không có thuốc để tạo đơn thuốc", actual_exception.getMessage());
        verify(mock_invoiceClient, never()).getInvoicesByAppointmentId(any(UUID.class));
        verify(mock_prescriptionAggregateRepository, never()).newInstance(any());

        // --- CheckDB: not applicable because validation fails before persistence ---
        // --- Rollback: not applicable because no repository write is attempted ---
    }

    // TC_PrescriptionCommandHandler_Handle_003
    @Test
    void test_handle_nullMedicineItems_throwsIllegalStateException() throws Exception {
        /*
         * Test Case ID : TC_PrescriptionCommandHandler_Handle_003
         * Objective    : Verify null input handling for medicine items.
         * Input        : CreatePrescriptionCommand.items=null.
         * Expected     : Throws IllegalStateException and does not call InvoiceClient or Repository.
         */
        // --- Arrange: set up command with null medicine item list ---
        CreatePrescriptionCommand command_nullItems = new CreatePrescriptionCommand(
                UUID.fromString("12121212-aaaa-1212-aaaa-121212121212"),
                UUID.fromString("23232323-bbbb-2323-bbbb-232323232323"),
                UUID.fromString("34343434-cccc-3434-cccc-343434343434"),
                UUID.fromString("45454545-dddd-4545-dddd-454545454545"),
                UUID.fromString("56565656-eeee-5656-eeee-565656565656"),
                null
        );

        // --- Act & Assert: verify the null input is rejected ---
        IllegalStateException actual_exception = assertThrows(
                IllegalStateException.class,
                () -> prescriptionCommandHandler.handle(command_nullItems)
        );
        assertEquals("Không có thuốc để tạo đơn thuốc", actual_exception.getMessage());
        verify(mock_invoiceClient, never()).getInvoicesByAppointmentId(any(UUID.class));
        verify(mock_prescriptionAggregateRepository, never()).newInstance(any());

        // --- CheckDB: not applicable because validation fails before persistence ---
        // --- Rollback: not applicable because no repository write is attempted ---
    }

    // TC_PrescriptionCommandHandler_Handle_004
    @Test
    void test_handle_invoiceClientThrows_propagatesException() throws Exception {
        /*
         * Test Case ID : TC_PrescriptionCommandHandler_Handle_004
         * Objective    : Verify dependency failure behavior when invoice lookup fails.
         * Input        : Valid command, InvoiceClient throws IllegalStateException.
         * Expected     : Throws same IllegalStateException and does not create aggregate.
         */
        // --- Arrange: set up valid command and failing invoice dependency ---
        UUID uuid_appointmentId = UUID.fromString("77777777-7777-7777-7777-777777777777");
        CreatePrescriptionCommand command_validPrescription = new CreatePrescriptionCommand(
                UUID.fromString("13131313-aaaa-1313-aaaa-131313131313"),
                uuid_appointmentId,
                UUID.fromString("24242424-bbbb-2424-bbbb-242424242424"),
                UUID.fromString("35353535-cccc-3535-cccc-353535353535"),
                UUID.fromString("46464646-dddd-4646-dddd-464646464646"),
                List.of(new MedicineItem(
                        UUID.fromString("57575757-eeee-5757-eeee-575757575757"),
                        UUID.fromString("68686868-ffff-6868-ffff-686868686868"),
                        "Cetirizine",
                        1,
                        6000,
                        "tablet",
                        "10mg",
                        "once daily",
                        "2 days",
                        "before sleep"
                ))
        );
        IllegalStateException expected_exception = new IllegalStateException("invoice unavailable");
        when(mock_invoiceClient.getInvoicesByAppointmentId(uuid_appointmentId)).thenThrow(expected_exception);

        // --- Act & Assert: verify dependency exception is propagated ---
        IllegalStateException actual_exception = assertThrows(
                IllegalStateException.class,
                () -> prescriptionCommandHandler.handle(command_validPrescription)
        );
        assertSame(expected_exception, actual_exception);
        verify(mock_invoiceClient).getInvoicesByAppointmentId(uuid_appointmentId);
        verify(mock_prescriptionAggregateRepository, never()).newInstance(any());

        // --- CheckDB: not applicable because repository write is not reached ---
        // --- Rollback: not applicable because no repository write is attempted ---
    }

    // TC_PrescriptionCommandHandler_Handle_005
    @Test
    void test_handle_repositoryThrows_wrapsExceptionInRuntimeException() throws Exception {
        /*
         * Test Case ID : TC_PrescriptionCommandHandler_Handle_005
         * Objective    : Verify repository creation failure is wrapped in RuntimeException.
         * Input        : Valid command, Repository.newInstance throws checked Exception.
         * Expected     : RuntimeException with original exception as cause.
         */
        // --- Arrange: set up valid command, invoice data, mapped items, and repository failure ---
        UUID uuid_appointmentId = UUID.fromString("14141414-1414-1414-1414-141414141414");
        CreatePrescriptionCommand command_validPrescription = new CreatePrescriptionCommand(
                UUID.fromString("15151515-1515-1515-1515-151515151515"),
                uuid_appointmentId,
                UUID.fromString("16161616-1616-1616-1616-161616161616"),
                UUID.fromString("17171717-1717-1717-1717-171717171717"),
                UUID.fromString("18181818-1818-1818-1818-181818181818"),
                List.of(new MedicineItem(
                        UUID.fromString("19191919-1919-1919-1919-191919191919"),
                        UUID.fromString("20202020-2121-2020-2121-202020202020"),
                        "Vitamin C",
                        5,
                        3000,
                        "tablet",
                        "500mg",
                        "once daily",
                        "5 days",
                        "after breakfast"
                ))
        );
        InvoiceResponseDTO invoiceResponseDTO_existingInvoice = new InvoiceResponseDTO();
        invoiceResponseDTO_existingInvoice.setId(UUID.fromString("21212121-2121-2121-2121-212121212121"));
        invoiceResponseDTO_existingInvoice.setItems(List.of());
        Exception expected_cause = new Exception("event store unavailable");
        when(mock_invoiceClient.getInvoicesByAppointmentId(uuid_appointmentId))
                .thenReturn(List.of(invoiceResponseDTO_existingInvoice));
        when(mock_invoiceItemMapper.toCheckerRequests(invoiceResponseDTO_existingInvoice.getItems()))
                .thenReturn(List.of());
        when(mock_prescriptionAggregateRepository.newInstance(any())).thenThrow(expected_cause);

        // --- Act & Assert: verify repository exception is wrapped as implemented ---
        RuntimeException actual_exception = assertThrows(
                RuntimeException.class,
                () -> prescriptionCommandHandler.handle(command_validPrescription)
        );
        assertSame(expected_cause, actual_exception.getCause());
        verify(mock_invoiceClient).getInvoicesByAppointmentId(uuid_appointmentId);
        verify(mock_invoiceItemMapper).toCheckerRequests(invoiceResponseDTO_existingInvoice.getItems());
        verify(mock_prescriptionAggregateRepository).newInstance(any());

        // --- CheckDB: not applicable because repository is mocked and no DB is accessed in this unit test ---
        // --- Rollback: not applicable because failed repository write is isolated by Mockito mock ---
    }

    // TC_PrescriptionCommandHandler_Handle_006
    @Test
    void test_handle_emptyInvoiceList_throwsIndexOutOfBoundsException() throws Exception {
        /*
         * Test Case ID : TC_PrescriptionCommandHandler_Handle_006
         * Objective    : Verify failure path when InvoiceClient returns no invoices for appointment.
         * Input        : Valid command, InvoiceClient returns [].
         * Expected     : IndexOutOfBoundsException is thrown and repository is not called.
         */
        // --- Arrange: set up valid command and empty invoice response ---
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

        // --- Act & Assert: verify empty invoice list causes current implementation failure ---
        assertThrows(
                IndexOutOfBoundsException.class,
                () -> prescriptionCommandHandler.handle(command_validPrescription)
        );
        verify(mock_invoiceClient).getInvoicesByAppointmentId(uuid_appointmentId);
        verify(mock_invoiceItemMapper, never()).toCheckerRequests(any());
        verify(mock_prescriptionAggregateRepository, never()).newInstance(any());

        // --- CheckDB: not applicable because repository write is not reached ---
        // --- Rollback: not applicable because no repository write is attempted ---
    }

    // TC_PrescriptionCommandHandler_Handle_007
    @Test
    void test_handle_mapperThrows_propagatesException() throws Exception {
        /*
         * Test Case ID : TC_PrescriptionCommandHandler_Handle_007
         * Objective    : Verify failure path when invoice item mapping fails.
         * Input        : Valid command and invoice response; InvoiceItemMapper throws IllegalArgumentException.
         * Expected     : Same IllegalArgumentException is propagated and repository is not called.
         */
        // --- Arrange: set up valid command, invoice response, and mapper failure ---
        UUID uuid_appointmentId = UUID.fromString("38383838-3838-3838-3838-383838383838");
        CreatePrescriptionCommand command_validPrescription = new CreatePrescriptionCommand(
                UUID.fromString("39393939-3939-3939-3939-393939393939"),
                uuid_appointmentId,
                UUID.fromString("40404040-4141-4040-4141-404040404040"),
                UUID.fromString("42424242-4343-4242-4343-424242424242"),
                UUID.fromString("44444444-4545-4444-4545-444444444444"),
                List.of(new MedicineItem(
                        UUID.fromString("46464646-4747-4646-4747-464646464646"),
                        UUID.fromString("48484848-4949-4848-4949-484848484848"),
                        "Metformin",
                        2,
                        15000,
                        "tablet",
                        "500mg",
                        "twice daily",
                        "7 days",
                        "with meals"
                ))
        );
        InvoiceItemResponseDTO invoiceItemResponseDTO_serviceCharge = new InvoiceItemResponseDTO();
        invoiceItemResponseDTO_serviceCharge.setDescription("X-ray");
        InvoiceResponseDTO invoiceResponseDTO_existingInvoice = new InvoiceResponseDTO();
        invoiceResponseDTO_existingInvoice.setId(UUID.fromString("50505050-5151-5050-5151-505050505050"));
        invoiceResponseDTO_existingInvoice.setItems(List.of(invoiceItemResponseDTO_serviceCharge));
        IllegalArgumentException expected_exception = new IllegalArgumentException("invalid invoice item");
        when(mock_invoiceClient.getInvoicesByAppointmentId(uuid_appointmentId))
                .thenReturn(List.of(invoiceResponseDTO_existingInvoice));
        when(mock_invoiceItemMapper.toCheckerRequests(invoiceResponseDTO_existingInvoice.getItems()))
                .thenThrow(expected_exception);

        // --- Act & Assert: verify mapper failure is propagated unchanged ---
        IllegalArgumentException actual_exception = assertThrows(
                IllegalArgumentException.class,
                () -> prescriptionCommandHandler.handle(command_validPrescription)
        );
        assertSame(expected_exception, actual_exception);
        verify(mock_invoiceClient).getInvoicesByAppointmentId(uuid_appointmentId);
        verify(mock_invoiceItemMapper).toCheckerRequests(invoiceResponseDTO_existingInvoice.getItems());
        verify(mock_prescriptionAggregateRepository, never()).newInstance(any());

        // --- CheckDB: not applicable because repository write is not reached ---
        // --- Rollback: not applicable because no repository write is attempted ---
    }
}
