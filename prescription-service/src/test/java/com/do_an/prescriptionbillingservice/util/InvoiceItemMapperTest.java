/*
 * =============================================================================
 * FILE: InvoiceItemMapperTest.java
 * MODULE UNDER TEST: InvoiceItemMapper.java
 * DESCRIPTION: Unit tests for MapStruct invoice item mapper behavior, including
 *              single item mapping, list mapping, empty list, and null input.
 * FRAMEWORK: JUnit 5
 * LIBRARIES: MapStruct, Spring Boot Test
 * AUTHOR: Student Team
 * DATE: 2026-05-12
 * =============================================================================
 */

package com.do_an.prescriptionbillingservice.util;

import com.do_an.common.model.InvoiceItemCheckerRequest;
import com.do_an.prescriptionbillingservice.dto.response.InvoiceItemResponseDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoiceItemMapperTest {

    private final InvoiceItemMapper invoiceItemMapper = Mappers.getMapper(InvoiceItemMapper.class);

    // TC_InvoiceItemMapper_ToCheckerRequest_001
    @Test
    void test_toCheckerRequest_validDto_mapsAllMatchingFields() {
        /*
         * Test Case ID : TC_InvoiceItemMapper_ToCheckerRequest_001
         * Objective    : Verify that matching fields from InvoiceItemResponseDTO are mapped to InvoiceItemCheckerRequest.
         * Input        : DTO with id, referenceId, serviceType, quantity, description, and unitPrice.
         * Expected     : Checker request has identical matching field values.
         */
        // --- Arrange: set up source DTO with all mapped fields ---
        UUID uuid_itemId = UUID.fromString("61616161-6161-6161-6161-616161616161");
        UUID uuid_referenceId = UUID.fromString("62626262-6262-6262-6262-626262626262");
        InvoiceItemResponseDTO dto_invoiceItem = new InvoiceItemResponseDTO();
        dto_invoiceItem.setId(uuid_itemId);
        dto_invoiceItem.setReferenceId(uuid_referenceId);
        dto_invoiceItem.setServiceType("SERVICE");
        dto_invoiceItem.setQuantity(2);
        dto_invoiceItem.setDescription("Tooth extraction");
        dto_invoiceItem.setUnitPrice(250000);

        // --- Act: map the DTO under test ---
        InvoiceItemCheckerRequest actual_checkerRequest = invoiceItemMapper.toCheckerRequest(dto_invoiceItem);

        // --- Assert: verify every matching mapped field ---
        assertEquals(uuid_itemId, actual_checkerRequest.getId());
        assertEquals(uuid_referenceId, actual_checkerRequest.getReferenceId());
        assertEquals("SERVICE", actual_checkerRequest.getServiceType());
        assertEquals(2, actual_checkerRequest.getQuantity());
        assertEquals("Tooth extraction", actual_checkerRequest.getDescription());
        assertEquals(250000, actual_checkerRequest.getUnitPrice());

        // --- CheckDB: not applicable because MapStruct mapping does not access DB ---
        // --- Rollback: not applicable because no DB state is modified ---
    }

    // TC_InvoiceItemMapper_ToCheckerRequests_001
    @Test
    void test_toCheckerRequests_validDtoList_mapsEveryItem() {
        /*
         * Test Case ID : TC_InvoiceItemMapper_ToCheckerRequests_001
         * Objective    : Verify that a DTO list is mapped to a new list with mapped item values.
         * Input        : List containing one InvoiceItemResponseDTO.
         * Expected     : Result list size is 1 and contains mapped values.
         */
        // --- Arrange: set up source list with one item ---
        InvoiceItemResponseDTO dto_invoiceItem = new InvoiceItemResponseDTO();
        dto_invoiceItem.setId(UUID.fromString("63636363-6363-6363-6363-636363636363"));
        dto_invoiceItem.setDescription("Dental filling");
        dto_invoiceItem.setQuantity(1);
        dto_invoiceItem.setUnitPrice(180000);
        List<InvoiceItemResponseDTO> list_invoiceItems = List.of(dto_invoiceItem);

        // --- Act: map the list under test ---
        List<InvoiceItemCheckerRequest> actual_checkerRequests =
                invoiceItemMapper.toCheckerRequests(list_invoiceItems);

        // --- Assert: verify list mapping creates mapped output ---
        assertNotSame(list_invoiceItems, actual_checkerRequests);
        assertEquals(1, actual_checkerRequests.size());
        assertEquals("Dental filling", actual_checkerRequests.get(0).getDescription());
        assertEquals(1, actual_checkerRequests.get(0).getQuantity());
        assertEquals(180000, actual_checkerRequests.get(0).getUnitPrice());

        // --- CheckDB: not applicable because MapStruct mapping does not access DB ---
        // --- Rollback: not applicable because no DB state is modified ---
    }

    // TC_InvoiceItemMapper_ToCheckerRequests_002
    @Test
    void test_toCheckerRequests_emptyList_returnsEmptyList() {
        /*
         * Test Case ID : TC_InvoiceItemMapper_ToCheckerRequests_002
         * Objective    : Verify boundary behavior for an empty input list.
         * Input        : dtoList=[].
         * Expected     : Returns an empty list.
         */
        // --- Arrange: set up empty source list ---
        List<InvoiceItemResponseDTO> list_emptyInvoiceItems = List.of();

        // --- Act: map the empty list under test ---
        List<InvoiceItemCheckerRequest> actual_checkerRequests =
                invoiceItemMapper.toCheckerRequests(list_emptyInvoiceItems);

        // --- Assert: verify empty output list ---
        assertTrue(actual_checkerRequests.isEmpty());

        // --- CheckDB: not applicable because MapStruct mapping does not access DB ---
        // --- Rollback: not applicable because no DB state is modified ---
    }

    // TC_InvoiceItemMapper_ToCheckerRequest_002
    @Test
    void test_toCheckerRequest_nullDto_returnsNull() {
        /*
         * Test Case ID : TC_InvoiceItemMapper_ToCheckerRequest_002
         * Objective    : Verify null input behavior generated by MapStruct.
         * Input        : dto=null.
         * Expected     : Returns null.
         */
        // --- Arrange: set up null DTO input ---
        InvoiceItemResponseDTO dto_nullInvoiceItem = null;

        // --- Act: map the null DTO under test ---
        InvoiceItemCheckerRequest actual_checkerRequest =
                invoiceItemMapper.toCheckerRequest(dto_nullInvoiceItem);

        // --- Assert: verify null output ---
        assertNull(actual_checkerRequest);

        // --- CheckDB: not applicable because MapStruct mapping does not access DB ---
        // --- Rollback: not applicable because no DB state is modified ---
    }
}
