/*
 * =============================================================================
 * FILE: PrescriptionServiceTest.java
 * MODULE UNDER TEST: PrescriptionService.java
 * DESCRIPTION: Unit tests for PrescriptionService status lookup behavior,
 *              including dependency delegation, null input, empty response,
 *              and dependency failure cases.
 * FRAMEWORK: JUnit 5
 * LIBRARIES: Mockito, Spring Boot Test
 * AUTHOR: Student Team
 * DATE: 2026-05-12
 * =============================================================================
 */

package com.do_an.prescriptionbillingservice.service;

import com.do_an.prescriptionbillingservice.client.InventoryClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private InventoryClient mock_inventoryClient;

    @InjectMocks
    private PrescriptionService prescriptionService;

    // TC_PrescriptionService_GetPrescriptionStatus_001
    @Test
    void test_getPrescriptionStatus_validMedicalHistoryId_returnsInventoryStatus() {
        /*
         * Test Case ID : TC_PrescriptionService_GetPrescriptionStatus_001
         * Objective    : Verify that prescription status is returned for a valid medical history id.
         * Input        : medicalHistoryId=11111111-1111-1111-1111-111111111111.
         * Expected     : Returns the exact status map from InventoryClient.
         */
        // --- Arrange: set up input data and dependency behavior ---
        UUID uuid_validMedicalHistoryId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Map<String, Object> expected_status = Map.of("status", "RELEASED", "dispenseOrderId", "DO-001");
        when(mock_inventoryClient.getPrescriptionStatusByMedicalHistoryId(uuid_validMedicalHistoryId))
                .thenReturn(expected_status);

        // --- Act: call the function under test ---
        Map<String, Object> actual_status = prescriptionService.getPrescriptionStatus(uuid_validMedicalHistoryId);

        // --- Assert: verify expected output and dependency call ---
        assertSame(expected_status, actual_status);
        verify(mock_inventoryClient).getPrescriptionStatusByMedicalHistoryId(uuid_validMedicalHistoryId);

        // --- CheckDB: not applicable because this unit delegates to a Feign client and does not access DB ---
        // --- Rollback: not applicable because no DB state is modified ---
    }

    // TC_PrescriptionService_GetPrescriptionStatus_002
    @Test
    void test_getPrescriptionStatus_unknownMedicalHistoryId_returnsEmptyMap() {
        /*
         * Test Case ID : TC_PrescriptionService_GetPrescriptionStatus_002
         * Objective    : Verify that an empty dependency response is propagated unchanged.
         * Input        : medicalHistoryId=22222222-2222-2222-2222-222222222222.
         * Expected     : Returns Collections.emptyMap().
         */
        // --- Arrange: set up an unknown medical history id and empty response ---
        UUID uuid_unknownMedicalHistoryId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Map<String, Object> expected_emptyStatus = Collections.emptyMap();
        when(mock_inventoryClient.getPrescriptionStatusByMedicalHistoryId(uuid_unknownMedicalHistoryId))
                .thenReturn(expected_emptyStatus);

        // --- Act: call the function under test ---
        Map<String, Object> actual_status = prescriptionService.getPrescriptionStatus(uuid_unknownMedicalHistoryId);

        // --- Assert: verify the empty response is not transformed ---
        assertSame(expected_emptyStatus, actual_status);
        verify(mock_inventoryClient).getPrescriptionStatusByMedicalHistoryId(uuid_unknownMedicalHistoryId);

        // --- CheckDB: not applicable because this unit delegates to a Feign client and does not access DB ---
        // --- Rollback: not applicable because no DB state is modified ---
    }

    // TC_PrescriptionService_GetPrescriptionStatus_003
    @Test
    void test_getPrescriptionStatus_nullMedicalHistoryId_delegatesNullInput() {
        /*
         * Test Case ID : TC_PrescriptionService_GetPrescriptionStatus_003
         * Objective    : Verify current boundary behavior when medicalHistoryId is null.
         * Input        : medicalHistoryId=null.
         * Expected     : Returns the exact status map from InventoryClient for null input.
         */
        // --- Arrange: set up null input and mocked dependency response ---
        UUID uuid_nullMedicalHistoryId = null;
        Map<String, Object> expected_status = Map.of("status", "NONE");
        when(mock_inventoryClient.getPrescriptionStatusByMedicalHistoryId(uuid_nullMedicalHistoryId))
                .thenReturn(expected_status);

        // --- Act: call the function under test ---
        Map<String, Object> actual_status = prescriptionService.getPrescriptionStatus(uuid_nullMedicalHistoryId);

        // --- Assert: verify null input is delegated according to current implementation ---
        assertSame(expected_status, actual_status);
        verify(mock_inventoryClient).getPrescriptionStatusByMedicalHistoryId(uuid_nullMedicalHistoryId);

        // --- CheckDB: not applicable because this unit delegates to a Feign client and does not access DB ---
        // --- Rollback: not applicable because no DB state is modified ---
    }

    // TC_PrescriptionService_GetPrescriptionStatus_004
    @Test
    void test_getPrescriptionStatus_inventoryClientThrows_propagatesException() {
        /*
         * Test Case ID : TC_PrescriptionService_GetPrescriptionStatus_004
         * Objective    : Verify failure path when InventoryClient cannot fetch prescription status.
         * Input        : medicalHistoryId=33333333-3333-3333-3333-333333333333.
         * Expected     : Throws IllegalStateException from dependency.
         */
        // --- Arrange: set up dependency failure ---
        UUID uuid_validMedicalHistoryId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        IllegalStateException expected_exception = new IllegalStateException("inventory unavailable");
        when(mock_inventoryClient.getPrescriptionStatusByMedicalHistoryId(uuid_validMedicalHistoryId))
                .thenThrow(expected_exception);

        // --- Act & Assert: verify the exception type and instance are propagated ---
        IllegalStateException actual_exception = assertThrows(
                IllegalStateException.class,
                () -> prescriptionService.getPrescriptionStatus(uuid_validMedicalHistoryId)
        );
        assertSame(expected_exception, actual_exception);
        verify(mock_inventoryClient).getPrescriptionStatusByMedicalHistoryId(uuid_validMedicalHistoryId);

        // --- CheckDB: not applicable because this unit delegates to a Feign client and does not access DB ---
        // --- Rollback: not applicable because no DB state is modified ---
    }

    // TC_PrescriptionService_GetPrescriptionStatus_005
    @Test
    void test_getPrescriptionStatus_inventoryClientReturnsNull_returnsNull() {
        /*
         * Test Case ID : TC_PrescriptionService_GetPrescriptionStatus_005
         * Objective    : Verify current boundary behavior when InventoryClient returns null.
         * Input        : medicalHistoryId=44444444-4444-4444-4444-444444444444; InventoryClient returns null.
         * Expected     : Returns null because current service implementation does not transform dependency output.
         */
        // --- Arrange: set up dependency response as null ---
        UUID uuid_validMedicalHistoryId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        when(mock_inventoryClient.getPrescriptionStatusByMedicalHistoryId(uuid_validMedicalHistoryId))
                .thenReturn(null);

        // --- Act: call the function under test ---
        Map<String, Object> actual_status = prescriptionService.getPrescriptionStatus(uuid_validMedicalHistoryId);

        // --- Assert: verify null response is propagated unchanged ---
        assertNull(actual_status);
        verify(mock_inventoryClient).getPrescriptionStatusByMedicalHistoryId(uuid_validMedicalHistoryId);

        // --- CheckDB: not applicable because this unit delegates to a Feign client and does not access DB ---
        // --- Rollback: not applicable because no DB state is modified ---
    }
}
