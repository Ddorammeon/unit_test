/*
 * =============================================================================
 * FILE: EntityMapperTest.java
 * MODULE UNDER TEST: EntityMapper.java
 * DESCRIPTION: Unit tests for DTO/entity mapping behavior used by patient and
 *              medical history workflows.
 * FRAMEWORK: JUnit 5
 * LIBRARIES: Spring Boot Test
 * AUTHOR: Student Team
 * DATE: 2026-05-12
 * =============================================================================
 */

package com.main_project.patient_service.util;

import com.main_project.patient_service.dto.ConditionRequestDTO;
import com.main_project.patient_service.dto.MedicalHistoryRequestDTO;
import com.main_project.patient_service.dto.PatientRequestDTO;
import com.main_project.patient_service.dto.PatientResponseDTO;
import com.main_project.patient_service.entity.Allergy;
import com.main_project.patient_service.entity.MedicalHistory;
import com.main_project.patient_service.entity.Patient;
import com.main_project.patient_service.enums.BloodType;
import com.main_project.patient_service.enums.Gender;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class EntityMapperTest {

    private final EntityMapper entityMapper = new EntityMapper();

    // TC_EntityMapper_ToPatientResponse_001
    @Test
    void test_toPatientResponse_patientWithChildren_mapsNestedLists() {
        /*
         * Test Case ID : TC_EntityMapper_ToPatientResponse_001
         * Objective    : Verify patient entity with child aggregate data maps to response DTO.
         * Input        : Patient with one allergy, one disease, and one tooth issue.
         * Expected     : Response DTO contains matching nested list values.
         */
        // --- Arrange: set up patient aggregate with children ---
        UUID uuid_userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID uuid_allergyId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Patient entity_patient = Patient.builder()
                .userId(uuid_userId)
                .dob(LocalDate.of(1995, 5, 20))
                .gender(Gender.FEMALE)
                .bloodType(BloodType.A_POSITIVE)
                .address("Mapper address")
                .contactPhone("0900000000")
                .insuranceNumber("INS-100")
                .build();
        entity_patient.addPatientAllergy(Allergy.builder().id(uuid_allergyId).name("Seafood").build(), "SEVERE", "Itchy", null);
        entity_patient.addUnderlyingDisease("Asthma", "ACTIVE", "MILD", true, null);
        entity_patient.addToothIssue(3, "ACTIVE", "Cavity", LocalDate.of(2026, 1, 1), "Need check");

        // --- Act: map entity to DTO ---
        PatientResponseDTO actual_result = entityMapper.toPatientResponse(entity_patient);

        // --- Assert: verify scalar and nested mapped values ---
        assertEquals(uuid_userId, actual_result.getUserId());
        assertEquals("FEMALE", actual_result.getGender());
        assertEquals("A_POSITIVE", actual_result.getBloodType());
        assertEquals("Seafood", actual_result.getPatientAllergies().get(0).getAllergyName());
        assertEquals("Asthma", actual_result.getUnderlyingDiseases().get(0).getName());
        assertEquals(3, actual_result.getToothIssues().get(0).getToothNumber());

        // --- CheckDB: not applicable because mapper does not access DB ---
        // --- Rollback: not applicable because no DB state is modified ---
    }

    // TC_EntityMapper_ToPatientEntity_001
    @Test
    void test_toPatientEntity_validRequest_mapsEnumsAndFields() {
        /*
         * Test Case ID : TC_EntityMapper_ToPatientEntity_001
         * Objective    : Verify patient request maps enum string values into entity enum fields.
         * Input        : PatientRequestDTO(gender="MALE", bloodType="O_NEGATIVE").
         * Expected     : Patient.gender=MALE and bloodType=O_NEGATIVE.
         */
        // --- Arrange: set up valid patient request ---
        UUID uuid_userId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        PatientRequestDTO request_patient = PatientRequestDTO.builder()
                .userId(uuid_userId)
                .dob(LocalDate.of(2000, 1, 1))
                .gender("MALE")
                .bloodType("O_NEGATIVE")
                .address("Entity address")
                .build();

        // --- Act: map DTO to entity ---
        Patient actual_result = entityMapper.toPatientEntity(request_patient);

        // --- Assert: verify mapped entity fields ---
        assertEquals(uuid_userId, actual_result.getUserId());
        assertEquals(Gender.MALE, actual_result.getGender());
        assertEquals(BloodType.O_NEGATIVE, actual_result.getBloodType());

        // --- CheckDB: not applicable because mapper does not access DB ---
        // --- Rollback: not applicable because no DB state is modified ---
    }

    // TC_EntityMapper_ToMedicalHistoryEntity_001
    @Test
    void test_toMedicalHistoryEntity_requestWithConditions_mapsConditionSet() {
        /*
         * Test Case ID : TC_EntityMapper_ToMedicalHistoryEntity_001
         * Objective    : Verify medical history request maps conditions into condition entities.
         * Input        : MedicalHistoryRequestDTO with one ConditionRequestDTO.
         * Expected     : MedicalHistory has one Condition with generated id.
         */
        // --- Arrange: set up request with one condition ---
        Patient entity_patient = Patient.builder().userId(UUID.fromString("44444444-4444-4444-4444-444444444444")).build();
        ConditionRequestDTO request_condition = new ConditionRequestDTO();
        request_condition.setToothNumber(16);
        request_condition.setName("Decay");
        MedicalHistoryRequestDTO request_history = new MedicalHistoryRequestDTO();
        request_history.setAppointmentId(UUID.fromString("55555555-5555-5555-5555-555555555555"));
        request_history.setPatientId(entity_patient.getUserId());
        request_history.setSymptoms("Pain");
        request_history.setConditions(List.of(request_condition));

        // --- Act: map DTO to entity ---
        MedicalHistory actual_result = entityMapper.toMedicalHistoryEntity(request_history, entity_patient);

        // --- Assert: verify condition set is created and linked ---
        assertEquals(entity_patient, actual_result.getPatient());
        assertEquals(1, actual_result.getConditions().size());
        assertNotNull(actual_result.getConditions().iterator().next().getId());
        assertEquals(16, actual_result.getConditions().iterator().next().getToothNumber());

        // --- CheckDB: not applicable because mapper does not access DB ---
        // --- Rollback: not applicable because no DB state is modified ---
    }

    // TC_EntityMapper_NullInputs_001
    @Test
    void test_mapperMethods_nullInputs_returnNullOrNoChange() {
        /*
         * Test Case ID : TC_EntityMapper_NullInputs_001
         * Objective    : Verify mapper null input boundary behavior.
         * Input        : null patient, null request, null medical history.
         * Expected     : Mapping methods return null where applicable.
         */
        // --- Arrange: set up null inputs ---
        Patient entity_nullPatient = null;
        PatientRequestDTO request_nullPatient = null;
        MedicalHistory entity_nullHistory = null;

        // --- Act: call mapper methods under null boundary condition ---
        PatientResponseDTO actual_patientResponse = entityMapper.toPatientResponse(entity_nullPatient);
        Patient actual_patientEntity = entityMapper.toPatientEntity(request_nullPatient);
        Object actual_historyResponse = entityMapper.toMedicalHistoryResponse(entity_nullHistory);

        // --- Assert: verify null-safe mapping behavior ---
        assertNull(actual_patientResponse);
        assertNull(actual_patientEntity);
        assertNull(actual_historyResponse);

        // --- CheckDB: not applicable because mapper does not access DB ---
        // --- Rollback: not applicable because no DB state is modified ---
    }
}
