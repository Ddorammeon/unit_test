/*
 * =============================================================================
 * FILE: PrescriptionBillingControllerTest.java
 * MODULE UNDER TEST: PrescriptionBillingController.java
 * DESCRIPTION: Unit tests for prescription billing REST controller behavior,
 *              covering command dispatch and status lookup delegation.
 * FRAMEWORK: JUnit 5
 * LIBRARIES: Mockito, Spring Boot Test
 * AUTHOR: Student Team
 * DATE: 2026-05-12
 * =============================================================================
 */

package com.do_an.prescriptionbillingservice.controller;

import com.do_an.common.command.CreatePrescriptionCommand;
import com.do_an.common.model.MedicineItem;
import com.do_an.prescriptionbillingservice.dto.CreatePrescriptionRequest;
import com.do_an.prescriptionbillingservice.service.PrescriptionService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrescriptionBillingControllerTest {

    @Mock
    private CommandGateway mock_commandGateway;

    @Mock
    private PrescriptionService mock_prescriptionService;

    @InjectMocks
    private PrescriptionBillingController prescriptionBillingController;

    // TC_PrescriptionBillingController_CreatePrescription_001
    @Test
    void test_createPrescription_validRequest_returnsAcceptedAndSendsCommand() {
        /*
         * Test Case ID : TC_PrescriptionBillingController_CreatePrescription_001
         * Objective    : Verify that a valid request is accepted and converted into CreatePrescriptionCommand.
         * Input        : request with appointmentId, patientId, doctorId, medicalHistoryId, and one medicine item.
         * Expected     : HTTP 202, response body contains generated id, CommandGateway receives matching command.
         */
        // --- Arrange: set up request data and command captor ---
        UUID uuid_appointmentId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID uuid_patientId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        UUID uuid_doctorId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
        UUID uuid_medicalHistoryId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
        List<MedicineItem> list_validMedicineItems = List.of(new MedicineItem(
                UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"),
                UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"),
                "Paracetamol",
                2,
                5000,
                "tablet",
                "500mg",
                "twice daily",
                "3 days",
                "after meals"
        ));
        CreatePrescriptionRequest request_validPrescription = new CreatePrescriptionRequest();
        request_validPrescription.setAppointmentId(uuid_appointmentId);
        request_validPrescription.setPatientId(uuid_patientId);
        request_validPrescription.setDoctorId(uuid_doctorId);
        request_validPrescription.setMedicalHistoryId(uuid_medicalHistoryId);
        request_validPrescription.setItems(list_validMedicineItems);
        ArgumentCaptor<CreatePrescriptionCommand> captor_createPrescriptionCommand =
                ArgumentCaptor.forClass(CreatePrescriptionCommand.class);

        // --- Act: call the controller method under test ---
        ResponseEntity<?> actual_response = prescriptionBillingController.createPrescription(request_validPrescription);

        // --- Assert: verify HTTP response and command payload ---
        assertEquals(HttpStatus.ACCEPTED, actual_response.getStatusCode());
        assertInstanceOf(Map.class, actual_response.getBody());
        Map<?, ?> actual_body = (Map<?, ?>) actual_response.getBody();
        assertNotNull(actual_body.get("id"));
        assertEquals("Đang xử lý đơn thuốc", actual_body.get("message"));
        verify(mock_commandGateway).send(captor_createPrescriptionCommand.capture());
        CreatePrescriptionCommand actual_command = captor_createPrescriptionCommand.getValue();
        assertNotNull(actual_command.getPrescriptionId());
        assertEquals(uuid_appointmentId, actual_command.getAppointmentId());
        assertEquals(uuid_patientId, actual_command.getPatientId());
        assertEquals(uuid_doctorId, actual_command.getDoctorId());
        assertEquals(uuid_medicalHistoryId, actual_command.getMedicalHistoryId());
        assertSame(list_validMedicineItems, actual_command.getItems());

        // --- CheckDB: not applicable because this controller dispatches a command and does not access DB directly ---
        // --- Rollback: not applicable because no DB state is modified in this unit test ---
    }

    // TC_PrescriptionBillingController_GetStatus_001
    @Test
    void test_getStatus_validMedicalHistoryId_returnsServiceStatus() {
        /*
         * Test Case ID : TC_PrescriptionBillingController_GetStatus_001
         * Objective    : Verify that getStatus returns OK with the service response body.
         * Input        : medicalHistoryId=99999999-9999-9999-9999-999999999999.
         * Expected     : HTTP 200 and exact status map from PrescriptionService.
         */
        // --- Arrange: set up service response ---
        UUID uuid_validMedicalHistoryId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        Map<String, Object> expected_status = Map.of("status", "SOLD");
        when(mock_prescriptionService.getPrescriptionStatus(uuid_validMedicalHistoryId)).thenReturn(expected_status);

        // --- Act: call the controller method under test ---
        ResponseEntity<Map<String, Object>> actual_response =
                prescriptionBillingController.getStatus(uuid_validMedicalHistoryId);

        // --- Assert: verify response status, body identity, and dependency call ---
        assertEquals(HttpStatus.OK, actual_response.getStatusCode());
        assertSame(expected_status, actual_response.getBody());
        verify(mock_prescriptionService).getPrescriptionStatus(uuid_validMedicalHistoryId);

        // --- CheckDB: not applicable because this controller delegates status lookup to service ---
        // --- Rollback: not applicable because no DB state is modified ---
    }

    // TC_PrescriptionBillingController_CreatePrescription_002
    @Test
    void test_createPrescription_commandGatewayFailure_propagatesException() {
        /*
         * Test Case ID : TC_PrescriptionBillingController_CreatePrescription_002
         * Objective    : Verify failure path when command dispatch fails.
         * Input        : syntactically valid request, CommandGateway throws IllegalStateException.
         * Expected     : IllegalStateException is propagated to the caller.
         */
        // --- Arrange: set up a valid request and failing command gateway ---
        CreatePrescriptionRequest request_validPrescription = new CreatePrescriptionRequest();
        request_validPrescription.setAppointmentId(UUID.fromString("12121212-1212-1212-1212-121212121212"));
        request_validPrescription.setPatientId(UUID.fromString("23232323-2323-2323-2323-232323232323"));
        request_validPrescription.setDoctorId(UUID.fromString("34343434-3434-3434-3434-343434343434"));
        request_validPrescription.setMedicalHistoryId(UUID.fromString("45454545-4545-4545-4545-454545454545"));
        request_validPrescription.setItems(List.of(new MedicineItem(
                UUID.fromString("56565656-5656-5656-5656-565656565656"),
                UUID.fromString("67676767-6767-6767-6767-676767676767"),
                "Amoxicillin",
                1,
                12000,
                "capsule",
                "250mg",
                "three times daily",
                "5 days",
                "after meals"
        )));
        IllegalStateException expected_exception = new IllegalStateException("command bus unavailable");
        when(mock_commandGateway.send(any(CreatePrescriptionCommand.class))).thenThrow(expected_exception);

        // --- Act & Assert: verify the failure is not swallowed ---
        IllegalStateException actual_exception = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> prescriptionBillingController.createPrescription(request_validPrescription)
        );
        assertSame(expected_exception, actual_exception);
        verify(mock_commandGateway).send(any(CreatePrescriptionCommand.class));

        // --- CheckDB: not applicable because this controller dispatches a command and does not access DB directly ---
        // --- Rollback: not applicable because no DB state is modified in this unit test ---
    }

    // TC_PrescriptionBillingController_CreatePrescription_003
    @Test
    void test_createPrescription_emptyMedicineItems_stillDispatchesCommand() {
        /*
         * Test Case ID : TC_PrescriptionBillingController_CreatePrescription_003
         * Objective    : Verify current controller boundary behavior when medicine item list is empty.
         * Input        : request.items=[] with valid appointment, patient, doctor, and medical history ids.
         * Expected     : HTTP 202 and command is dispatched with empty items; validation is handled later by command handler.
         */
        // --- Arrange: set up request with empty medicine items and command captor ---
        CreatePrescriptionRequest request_emptyItems = new CreatePrescriptionRequest();
        request_emptyItems.setAppointmentId(UUID.fromString("78787878-7878-7878-7878-787878787878"));
        request_emptyItems.setPatientId(UUID.fromString("89898989-8989-8989-8989-898989898989"));
        request_emptyItems.setDoctorId(UUID.fromString("90909090-aaaa-9090-aaaa-909090909090"));
        request_emptyItems.setMedicalHistoryId(UUID.fromString("91919191-bbbb-9191-bbbb-919191919191"));
        request_emptyItems.setItems(List.of());
        ArgumentCaptor<CreatePrescriptionCommand> captor_createPrescriptionCommand =
                ArgumentCaptor.forClass(CreatePrescriptionCommand.class);

        // --- Act: call the controller method under test ---
        ResponseEntity<?> actual_response = prescriptionBillingController.createPrescription(request_emptyItems);

        // --- Assert: verify response and command preserve empty list ---
        assertEquals(HttpStatus.ACCEPTED, actual_response.getStatusCode());
        verify(mock_commandGateway).send(captor_createPrescriptionCommand.capture());
        assertEquals(List.of(), captor_createPrescriptionCommand.getValue().getItems());

        // --- CheckDB: not applicable because this controller dispatches a command and does not access DB directly ---
        // --- Rollback: not applicable because no DB state is modified in this unit test ---
    }

    // TC_PrescriptionBillingController_CreatePrescription_004
    @Test
    void test_createPrescription_nullRequest_throwsNullPointerException() {
        /*
         * Test Case ID : TC_PrescriptionBillingController_CreatePrescription_004
         * Objective    : Verify failure path when request body is null.
         * Input        : request=null.
         * Expected     : NullPointerException is thrown and no command is dispatched.
         */
        // --- Arrange: set up null request input ---
        CreatePrescriptionRequest request_nullPrescription = null;

        // --- Act & Assert: verify null request is not silently accepted ---
        assertThrows(
                NullPointerException.class,
                () -> prescriptionBillingController.createPrescription(request_nullPrescription)
        );
        verify(mock_commandGateway, never()).send(any(CreatePrescriptionCommand.class));

        // --- CheckDB: not applicable because validation fails before command dispatch ---
        // --- Rollback: not applicable because no DB state is modified in this unit test ---
    }

    // TC_PrescriptionBillingController_GetStatus_002
    @Test
    void test_getStatus_serviceThrows_propagatesException() {
        /*
         * Test Case ID : TC_PrescriptionBillingController_GetStatus_002
         * Objective    : Verify failure path when PrescriptionService cannot fetch status.
         * Input        : medicalHistoryId=92929292-9292-9292-9292-929292929292; service throws IllegalStateException.
         * Expected     : Same IllegalStateException is propagated.
         */
        // --- Arrange: set up service failure ---
        UUID uuid_validMedicalHistoryId = UUID.fromString("92929292-9292-9292-9292-929292929292");
        IllegalStateException expected_exception = new IllegalStateException("status lookup unavailable");
        when(mock_prescriptionService.getPrescriptionStatus(uuid_validMedicalHistoryId)).thenThrow(expected_exception);

        // --- Act & Assert: verify exception propagation ---
        IllegalStateException actual_exception = assertThrows(
                IllegalStateException.class,
                () -> prescriptionBillingController.getStatus(uuid_validMedicalHistoryId)
        );
        assertSame(expected_exception, actual_exception);
        verify(mock_prescriptionService).getPrescriptionStatus(uuid_validMedicalHistoryId);

        // --- CheckDB: not applicable because this controller delegates status lookup to service ---
        // --- Rollback: not applicable because no DB state is modified ---
    }
}
