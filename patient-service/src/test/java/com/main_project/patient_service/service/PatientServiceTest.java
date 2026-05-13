/*
 * =============================================================================
 * FILE: PatientServiceTest.java
 * MODULE UNDER TEST: PatientService.java
 * DESCRIPTION: Unit tests for patient aggregate service business behavior,
 *              including create, update synchronization, read, delete, and
 *              invalid dependency cases.
 * FRAMEWORK: JUnit 5
 * LIBRARIES: Mockito, Spring Boot Test
 * AUTHOR: Student Team
 * DATE: 2026-05-12
 * =============================================================================
 */

package com.main_project.patient_service.service;

import com.main_project.patient_service.dto.PatientAllergyDTO;
import com.main_project.patient_service.dto.PatientRequestDTO;
import com.main_project.patient_service.dto.PatientResponseDTO;
import com.main_project.patient_service.dto.ToothIssueDTO;
import com.main_project.patient_service.dto.UnderlyingDiseaseDTO;
import com.main_project.patient_service.entity.Allergy;
import com.main_project.patient_service.entity.Patient;
import com.main_project.patient_service.repository.AllergyRepository;
import com.main_project.patient_service.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository mock_patientRepository;

    @Mock
    private AllergyRepository mock_allergyRepository;

    @InjectMocks
    private PatientService patientService;

    // TC_PatientService_CreatePatient_001
    @Test
    void test_createPatient_validFullRequest_savesAggregateWithChildren() {
        /*
         * Test Case ID : TC_PatientService_CreatePatient_001
         * Objective    : Verify valid patient data creates an aggregate with allergy, disease, and tooth issue children.
         * Input        : PatientRequestDTO with one allergy, one underlying disease, and one tooth issue.
         * Expected     : Repository saves patient and response contains all child collections.
         */
        // --- Arrange: set up complete request and allergy master lookup ---
        UUID uuid_userId = UUID.fromString("10101010-1010-1010-1010-101010101010");
        UUID uuid_allergyId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Allergy entity_allergy = Allergy.builder().id(uuid_allergyId).name("Penicillin").type("DRUG").build();
        PatientRequestDTO request_validPatient = buildPatientRequest(uuid_userId);
        request_validPatient.setPatientAllergies(List.of(PatientAllergyDTO.builder()
                .allergyId(uuid_allergyId)
                .severity("SEVERE")
                .reaction("Rash")
                .note("Avoid antibiotic")
                .build()));
        request_validPatient.setUnderlyingDiseases(List.of(UnderlyingDiseaseDTO.builder()
                .name("Diabetes")
                .status("CONTROLLED")
                .severity("MODERATE")
                .isVerified(true)
                .note("Monitor glucose")
                .build()));
        request_validPatient.setToothIssues(List.of(ToothIssueDTO.builder()
                .toothNumber(12)
                .status("ACTIVE")
                .description("Cavity")
                .diagnosedDate(LocalDate.of(2026, 1, 10))
                .note("Needs filling")
                .build()));
        when(mock_allergyRepository.findById(uuid_allergyId)).thenReturn(Optional.of(entity_allergy));
        when(mock_patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // --- Act: call the function under test ---
        PatientResponseDTO actual_result = patientService.createPatient(request_validPatient);

        // --- Assert: verify aggregate data and children were mapped to response ---
        assertEquals(uuid_userId, actual_result.getUserId());
        assertEquals("MALE", actual_result.getGender());
        assertEquals("O_POSITIVE", actual_result.getBloodType());
        assertEquals(1, actual_result.getPatientAllergies().size());
        assertEquals("Penicillin", actual_result.getPatientAllergies().get(0).getAllergyName());
        assertEquals(1, actual_result.getUnderlyingDiseases().size());
        assertEquals(1, actual_result.getToothIssues().size());
        verify(mock_patientRepository).save(any(Patient.class));

        // --- CheckDB: repository save is mocked; verify save call represents DB insert of aggregate ---
        // --- Rollback: Mockito mock isolates DB state, so no persistent data is written ---
    }

    // TC_PatientService_CreatePatient_002
    @Test
    void test_createPatient_missingAllergyMaster_throwsEntityNotFoundException() {
        /*
         * Test Case ID : TC_PatientService_CreatePatient_002
         * Objective    : Verify patient creation fails when referenced allergy master data does not exist.
         * Input        : PatientRequestDTO.patientAllergies[0].allergyId missing from AllergyRepository.
         * Expected     : EntityNotFoundException and patient is not saved.
         */
        // --- Arrange: set up request with missing allergy id ---
        UUID uuid_userId = UUID.fromString("20202020-2020-2020-2020-202020202020");
        UUID uuid_missingAllergyId = UUID.fromString("21212121-2121-2121-2121-212121212121");
        PatientRequestDTO request_patient = buildPatientRequest(uuid_userId);
        request_patient.setPatientAllergies(List.of(PatientAllergyDTO.builder()
                .allergyId(uuid_missingAllergyId)
                .severity("MILD")
                .build()));
        when(mock_allergyRepository.findById(uuid_missingAllergyId)).thenReturn(Optional.empty());

        // --- Act & Assert: verify not-found error and no DB write ---
        assertThrows(
                EntityNotFoundException.class,
                () -> patientService.createPatient(request_patient)
        );
        verify(mock_patientRepository, never()).save(any(Patient.class));

        // --- CheckDB: repository lookup is mocked; verify no save call means no DB insert ---
        // --- Rollback: not applicable because no repository write is attempted ---
    }

    // TC_PatientService_UpdatePatient_001
    @Test
    void test_updatePatient_existingPatient_replacesProvidedChildCollections() {
        /*
         * Test Case ID : TC_PatientService_UpdatePatient_001
         * Objective    : Verify smart sync clears and replaces provided allergy, disease, and tooth issue lists.
         * Input        : Existing patient with old children; update request with new child lists.
         * Expected     : Response contains only new child values.
         */
        // --- Arrange: set up existing patient with old values and update request ---
        UUID uuid_userId = UUID.fromString("30303030-3030-3030-3030-303030303030");
        UUID uuid_oldAllergyId = UUID.fromString("31313131-3131-3131-3131-313131313131");
        UUID uuid_newAllergyId = UUID.fromString("32323232-3232-3232-3232-323232323232");
        Patient entity_existingPatient = buildPatientEntity(uuid_userId);
        entity_existingPatient.addPatientAllergy(Allergy.builder().id(uuid_oldAllergyId).name("Dust").build(), "MILD", "Sneeze", null);
        entity_existingPatient.addUnderlyingDisease("Old Disease", "ACTIVE", "MILD", false, null);
        entity_existingPatient.addToothIssue(5, "ACTIVE", "Old cavity", LocalDate.of(2025, 1, 1), null);
        PatientRequestDTO request_updatePatient = buildPatientRequest(uuid_userId);
        request_updatePatient.setAddress("New address");
        request_updatePatient.setPatientAllergies(List.of(PatientAllergyDTO.builder()
                .allergyId(uuid_newAllergyId)
                .severity("SEVERE")
                .reaction("Swelling")
                .build()));
        request_updatePatient.setUnderlyingDiseases(List.of(UnderlyingDiseaseDTO.builder()
                .name("Hypertension")
                .status("CONTROLLED")
                .severity("MODERATE")
                .isVerified(true)
                .build()));
        request_updatePatient.setToothIssues(List.of(ToothIssueDTO.builder()
                .toothNumber(8)
                .status("TREATED")
                .description("Filling")
                .diagnosedDate(LocalDate.of(2026, 2, 1))
                .build()));
        when(mock_patientRepository.findByIdWithDetails(uuid_userId)).thenReturn(Optional.of(entity_existingPatient));
        when(mock_allergyRepository.findById(uuid_newAllergyId))
                .thenReturn(Optional.of(Allergy.builder().id(uuid_newAllergyId).name("Latex").build()));
        when(mock_patientRepository.save(entity_existingPatient)).thenReturn(entity_existingPatient);

        // --- Act: call the function under test ---
        PatientResponseDTO actual_result = patientService.updatePatient(uuid_userId, request_updatePatient);

        // --- Assert: verify smart sync replaced old children ---
        assertEquals("New address", actual_result.getAddress());
        assertEquals(1, actual_result.getPatientAllergies().size());
        assertEquals("Latex", actual_result.getPatientAllergies().get(0).getAllergyName());
        assertEquals(1, actual_result.getUnderlyingDiseases().size());
        assertEquals("Hypertension", actual_result.getUnderlyingDiseases().get(0).getName());
        assertEquals(1, actual_result.getToothIssues().size());
        assertEquals(8, actual_result.getToothIssues().get(0).getToothNumber());

        // --- CheckDB: repository save is mocked; verify save call represents DB update ---
        // --- Rollback: Mockito mock isolates DB state, so no persistent data is written ---
    }

    // TC_PatientService_UpdatePatient_002
    @Test
    void test_updatePatient_nullChildCollections_keepsExistingChildren() {
        /*
         * Test Case ID : TC_PatientService_UpdatePatient_002
         * Objective    : Verify null child lists mean "keep existing children" during update.
         * Input        : request.patientAllergies=null, underlyingDiseases=null, toothIssues=null.
         * Expected     : Existing child counts remain unchanged.
         */
        // --- Arrange: set up existing patient and partial update request ---
        UUID uuid_userId = UUID.fromString("40404040-4040-4040-4040-404040404040");
        Patient entity_existingPatient = buildPatientEntity(uuid_userId);
        entity_existingPatient.addUnderlyingDisease("Asthma", "ACTIVE", "MILD", true, null);
        PatientRequestDTO request_partialUpdate = new PatientRequestDTO();
        request_partialUpdate.setAddress("Partial address");
        request_partialUpdate.setPatientAllergies(null);
        request_partialUpdate.setUnderlyingDiseases(null);
        request_partialUpdate.setToothIssues(null);
        when(mock_patientRepository.findByIdWithDetails(uuid_userId)).thenReturn(Optional.of(entity_existingPatient));
        when(mock_patientRepository.save(entity_existingPatient)).thenReturn(entity_existingPatient);

        // --- Act: call the function under test ---
        PatientResponseDTO actual_result = patientService.updatePatient(uuid_userId, request_partialUpdate);

        // --- Assert: verify basic update occurs and child collection is preserved ---
        assertEquals("Partial address", actual_result.getAddress());
        assertEquals(1, actual_result.getUnderlyingDiseases().size());
        assertEquals("Asthma", actual_result.getUnderlyingDiseases().get(0).getName());

        // --- CheckDB: repository save is mocked; verify save call represents DB update ---
        // --- Rollback: Mockito mock isolates DB state, so no persistent data is written ---
    }

    // TC_PatientService_GetPatientById_001
    @Test
    void test_getPatientById_existingUserId_returnsPatientResponse() {
        /*
         * Test Case ID : TC_PatientService_GetPatientById_001
         * Objective    : Verify patient lookup by user id returns mapped response.
         * Input        : id=50505050-5050-5050-5050-505050505050.
         * Expected     : PatientResponseDTO with same user id.
         */
        // --- Arrange: set up repository read result ---
        UUID uuid_userId = UUID.fromString("50505050-5050-5050-5050-505050505050");
        when(mock_patientRepository.findByUserId(uuid_userId)).thenReturn(Optional.of(buildPatientEntity(uuid_userId)));

        // --- Act: call the function under test ---
        PatientResponseDTO actual_result = patientService.getPatientById(uuid_userId);

        // --- Assert: verify mapped response ---
        assertEquals(uuid_userId, actual_result.getUserId());
        verify(mock_patientRepository).findByUserId(uuid_userId);

        // --- CheckDB: repository read is mocked; verify findByUserId call represents DB read ---
        // --- Rollback: not applicable because no DB state is modified ---
    }

    // TC_PatientService_DeletePatient_001
    @Test
    void test_deletePatient_existingId_deletesById() {
        /*
         * Test Case ID : TC_PatientService_DeletePatient_001
         * Objective    : Verify existing patient can be deleted.
         * Input        : id=60606060-6060-6060-6060-606060606060; existsById=true.
         * Expected     : deleteById is called once.
         */
        // --- Arrange: set up existing patient id ---
        UUID uuid_userId = UUID.fromString("60606060-6060-6060-6060-606060606060");
        when(mock_patientRepository.existsById(uuid_userId)).thenReturn(true);

        // --- Act: call the function under test ---
        patientService.deletePatient(uuid_userId);

        // --- Assert: verify delete call ---
        verify(mock_patientRepository).deleteById(uuid_userId);

        // --- CheckDB: repository delete is mocked; verify deleteById call represents DB delete ---
        // --- Rollback: Mockito mock isolates DB state, so no persistent data is deleted ---
    }

    // TC_PatientService_CreatePatient_003
    @Test
    void test_createPatient_invalidGender_throwsIllegalArgumentException() {
        /*
         * Test Case ID : TC_PatientService_CreatePatient_003
         * Objective    : Verify invalid gender value is rejected by enum conversion.
         * Input        : gender="INVALID".
         * Expected     : IllegalArgumentException and patient is not saved.
         */
        // --- Arrange: set up invalid gender value ---
        PatientRequestDTO request_invalidGender = buildPatientRequest(UUID.fromString("70707070-7070-7070-7070-707070707070"));
        request_invalidGender.setGender("INVALID");

        // --- Act & Assert: verify invalid business value is rejected ---
        assertThrows(
                IllegalArgumentException.class,
                () -> patientService.createPatient(request_invalidGender)
        );
        verify(mock_patientRepository, never()).save(any(Patient.class));

        // --- CheckDB: validation fails before repository save ---
        // --- Rollback: not applicable because no repository write is attempted ---
    }

    // TC_PatientService_CreatePatient_004
    @Test
    void test_createPatient_minimalValidRequest_savesWithoutChildren() {
        /*
         * Test Case ID : TC_PatientService_CreatePatient_004
         * Objective    : Verify minimal valid request with empty child lists can be saved.
         * Input        : PatientRequestDTO with user id, dob, gender, phone, and empty children.
         * Expected     : Response has zero allergy, disease, and tooth issue items.
         */
        // --- Arrange: set up minimal request and capture saved patient ---
        UUID uuid_userId = UUID.fromString("80808080-8080-8080-8080-808080808080");
        PatientRequestDTO request_minimalPatient = buildPatientRequest(uuid_userId);
        request_minimalPatient.setPatientAllergies(List.of());
        request_minimalPatient.setUnderlyingDiseases(List.of());
        request_minimalPatient.setToothIssues(List.of());
        ArgumentCaptor<Patient> captor_savedPatient = ArgumentCaptor.forClass(Patient.class);
        when(mock_patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // --- Act: call the function under test ---
        PatientResponseDTO actual_result = patientService.createPatient(request_minimalPatient);

        // --- Assert: verify no child records were added ---
        verify(mock_patientRepository).save(captor_savedPatient.capture());
        assertEquals(uuid_userId, captor_savedPatient.getValue().getUserId());
        assertTrue(actual_result.getPatientAllergies().isEmpty());
        assertTrue(actual_result.getUnderlyingDiseases().isEmpty());
        assertTrue(actual_result.getToothIssues().isEmpty());
        assertFalse(captor_savedPatient.getValue().getUserId().toString().isBlank());

        // --- CheckDB: repository save is mocked; verify save call represents DB insert ---
        // --- Rollback: Mockito mock isolates DB state, so no persistent data is written ---
    }

    private PatientRequestDTO buildPatientRequest(UUID uuid_userId) {
        return PatientRequestDTO.builder()
                .userId(uuid_userId)
                .dob(LocalDate.of(1990, 1, 1))
                .gender("MALE")
                .address("123 Test Street")
                .contactPhone("0901234567")
                .bloodType("O_POSITIVE")
                .insuranceNumber("INS-001")
                .patientAllergies(List.of())
                .underlyingDiseases(List.of())
                .toothIssues(List.of())
                .build();
    }

    private Patient buildPatientEntity(UUID uuid_userId) {
        return Patient.builder()
                .userId(uuid_userId)
                .dob(LocalDate.of(1990, 1, 1))
                .gender(com.main_project.patient_service.enums.Gender.MALE)
                .address("123 Test Street")
                .contactPhone("0901234567")
                .bloodType(com.main_project.patient_service.enums.BloodType.O_POSITIVE)
                .insuranceNumber("INS-001")
                .build();
    }
}
