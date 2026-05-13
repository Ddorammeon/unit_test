/*
 * =============================================================================
 * FILE: PatientAggregateTest.java
 * MODULE UNDER TEST: Patient.java
 * DESCRIPTION: Unit tests for Patient aggregate root child-management business
 *              methods.
 * FRAMEWORK: JUnit 5
 * LIBRARIES: Spring Boot Test
 * AUTHOR: Student Team
 * DATE: 2026-05-12
 * =============================================================================
 */

package com.main_project.patient_service.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatientAggregateTest {

    // TC_PatientAggregate_AddPatientAllergy_001
    @Test
    void test_addPatientAllergy_validAllergy_setsBackReference() {
        /*
         * Test Case ID : TC_PatientAggregate_AddPatientAllergy_001
         * Objective    : Verify adding allergy through aggregate sets child back-reference.
         * Input        : Allergy(id=11111111-1111-1111-1111-111111111111).
         * Expected     : Patient has one PatientAllergy whose patient reference is the aggregate.
         */
        // --- Arrange: set up aggregate and allergy master data ---
        Patient entity_patient = Patient.builder().userId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")).build();
        Allergy entity_allergy = Allergy.builder().id(UUID.fromString("11111111-1111-1111-1111-111111111111")).name("Milk").build();

        // --- Act: add allergy through aggregate method ---
        PatientAllergy actual_patientAllergy = entity_patient.addPatientAllergy(entity_allergy, "MILD", "Rash", null);

        // --- Assert: verify aggregate relationship ---
        assertEquals(1, entity_patient.getPatientAllergies().size());
        assertSame(entity_patient, actual_patientAllergy.getPatient());
        assertSame(entity_allergy, actual_patientAllergy.getAllergy());

        // --- CheckDB: not applicable because aggregate method does not access DB ---
        // --- Rollback: not applicable because no DB state is modified ---
    }

    // TC_PatientAggregate_AddPatientAllergy_002
    @Test
    void test_addPatientAllergy_nullAllergy_throwsIllegalArgumentException() {
        /*
         * Test Case ID : TC_PatientAggregate_AddPatientAllergy_002
         * Objective    : Verify allergy child cannot be created without allergy master reference.
         * Input        : allergy=null.
         * Expected     : IllegalArgumentException.
         */
        // --- Arrange: set up aggregate and null allergy ---
        Patient entity_patient = Patient.builder().userId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")).build();
        Allergy entity_nullAllergy = null;

        // --- Act & Assert: verify null allergy is rejected ---
        assertThrows(
                IllegalArgumentException.class,
                () -> entity_patient.addPatientAllergy(entity_nullAllergy, "MILD", null, null)
        );

        // --- CheckDB: not applicable because aggregate method does not access DB ---
        // --- Rollback: not applicable because no DB state is modified ---
    }

    // TC_PatientAggregate_AddToothIssue_001
    @Test
    void test_addToothIssue_validInput_setsBackReference() {
        /*
         * Test Case ID : TC_PatientAggregate_AddToothIssue_001
         * Objective    : Verify adding tooth issue through aggregate sets child back-reference.
         * Input        : toothNumber=18, status="ACTIVE".
         * Expected     : Patient has one ToothIssue whose patient reference is the aggregate.
         */
        // --- Arrange: set up aggregate ---
        Patient entity_patient = Patient.builder().userId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")).build();

        // --- Act: add tooth issue through aggregate method ---
        ToothIssue actual_toothIssue = entity_patient.addToothIssue(18, "ACTIVE", "Cavity", LocalDate.of(2026, 1, 1), null);

        // --- Assert: verify aggregate relationship ---
        assertEquals(1, entity_patient.getToothIssues().size());
        assertSame(entity_patient, actual_toothIssue.getPatient());
        assertEquals(18, actual_toothIssue.getToothNumber());

        // --- CheckDB: not applicable because aggregate method does not access DB ---
        // --- Rollback: not applicable because no DB state is modified ---
    }

    // TC_PatientAggregate_ClearUnderlyingDiseases_001
    @Test
    void test_clearUnderlyingDiseases_existingDiseases_removesAllItems() {
        /*
         * Test Case ID : TC_PatientAggregate_ClearUnderlyingDiseases_001
         * Objective    : Verify aggregate clear operation removes all underlying diseases.
         * Input        : Patient with two underlying diseases.
         * Expected     : underlyingDiseases collection is empty.
         */
        // --- Arrange: set up aggregate with two diseases ---
        Patient entity_patient = Patient.builder().userId(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd")).build();
        entity_patient.addUnderlyingDisease("Diabetes", "ACTIVE", "MILD", true, null);
        entity_patient.addUnderlyingDisease("Asthma", "CONTROLLED", "MILD", true, null);

        // --- Act: clear disease collection ---
        entity_patient.clearUnderlyingDiseases();

        // --- Assert: verify collection is empty ---
        assertTrue(entity_patient.getUnderlyingDiseases().isEmpty());

        // --- CheckDB: not applicable because aggregate method does not access DB ---
        // --- Rollback: not applicable because no DB state is modified ---
    }
}
