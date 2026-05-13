/*
 * =============================================================================
 * FILE: BusinessLogicFailingTest.java
 * MODULE UNDER TEST: PatientService.java, AllergyService.java,
 *                    MedicalHistoryService.java, Patient.java
 * DESCRIPTION: Opt-in business expectation tests that intentionally fail against
 *              current implementation to expose missing domain validation.
 * FRAMEWORK: JUnit 5
 * LIBRARIES: Mockito, Spring Boot Test
 * AUTHOR: Student Team
 * DATE: 2026-05-12
 * =============================================================================
 */

package com.main_project.patient_service;

import com.main_project.patient_service.dto.AllergyDTO;
import com.main_project.patient_service.dto.ConditionRequestDTO;
import com.main_project.patient_service.dto.MedicalHistoryRequestDTO;
import com.main_project.patient_service.dto.PatientRequestDTO;
import com.main_project.patient_service.entity.Allergy;
import com.main_project.patient_service.entity.MedicalHistory;
import com.main_project.patient_service.entity.Patient;
import com.main_project.patient_service.repository.AllergyRepository;
import com.main_project.patient_service.repository.MedicalHistoryRepository;
import com.main_project.patient_service.repository.PatientRepository;
import com.main_project.patient_service.service.AllergyService;
import com.main_project.patient_service.service.MedicalHistoryService;
import com.main_project.patient_service.service.PatientService;
import com.main_project.patient_service.util.EntityMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@EnabledIfSystemProperty(named = "includeBusinessLogicFailures", matches = "true")
class BusinessLogicFailingTest {

    @Mock
    private PatientRepository mock_patientRepository;

    @Mock
    private AllergyRepository mock_allergyRepository;

    @Mock
    private MedicalHistoryRepository mock_medicalHistoryRepository;

    @Mock
    private EntityMapper mock_mapper;

    @InjectMocks
    private PatientService patientService;

    @InjectMocks
    private AllergyService allergyService;

    @InjectMocks
    private MedicalHistoryService medicalHistoryService;

    // TC_BusinessLogic_PatientService_FutureDob_001
    @Test
    void test_createPatient_futureDob_shouldRejectBeforeSaving() {
        /*
         * Test Case ID : TC_BusinessLogic_PatientService_FutureDob_001
         * Objective    : Verify desired business rule that date of birth must be in the past at service layer.
         * Input        : PatientRequestDTO.dob=tomorrow.
         * Expected     : IllegalArgumentException and PatientRepository.save is not called.
         */
        // --- Arrange: set up request with future date of birth ---
        PatientRequestDTO request_futureDob = PatientRequestDTO.builder()
                .userId(UUID.fromString("11111111-aaaa-1111-aaaa-111111111111"))
                .dob(LocalDate.now().plusDays(1))
                .gender("MALE")
                .bloodType("O_POSITIVE")
                .patientAllergies(List.of())
                .underlyingDiseases(List.of())
                .toothIssues(List.of())
                .build();
        when(mock_patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // --- Act & Assert: expect business validation before DB write ---
        assertThrows(
                IllegalArgumentException.class,
                () -> patientService.createPatient(request_futureDob)
        );
        verify(mock_patientRepository, never()).save(any(Patient.class));

        // --- CheckDB: expected validation should happen before DB write ---
        // --- Rollback: not applicable because no repository write should be attempted ---
    }

    // TC_BusinessLogic_AllergyService_BlankName_001
    @Test
    void test_createAllergy_blankName_shouldRejectBeforeSaving() {
        /*
         * Test Case ID : TC_BusinessLogic_AllergyService_BlankName_001
         * Objective    : Verify desired business rule that allergy name is required at service layer.
         * Input        : AllergyDTO.name="".
         * Expected     : IllegalArgumentException and AllergyRepository.save is not called.
         */
        // --- Arrange: set up allergy DTO with blank name ---
        AllergyDTO dto_blankName = AllergyDTO.builder()
                .name("")
                .type("FOOD")
                .description("Invalid blank name")
                .build();
        when(mock_allergyRepository.save(any(Allergy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // --- Act & Assert: expect business validation before DB write ---
        assertThrows(
                IllegalArgumentException.class,
                () -> allergyService.createAllergy(dto_blankName)
        );
        verify(mock_allergyRepository, never()).save(any());

        // --- CheckDB: expected validation should happen before DB write ---
        // --- Rollback: not applicable because no repository write should be attempted ---
    }

    // TC_BusinessLogic_MedicalHistoryService_InvalidToothNumber_001
    @Test
    void test_createMedicalHistory_toothNumberGreaterThan32_shouldRejectBeforeSaving() {
        /*
         * Test Case ID : TC_BusinessLogic_MedicalHistoryService_InvalidToothNumber_001
         * Objective    : Verify desired dental rule that adult tooth number must be between 1 and 32.
         * Input        : ConditionRequestDTO.toothNumber=99.
         * Expected     : IllegalArgumentException and MedicalHistoryRepository.save is not called.
         */
        // --- Arrange: set up request with invalid tooth number ---
        MedicalHistoryRequestDTO request_history = new MedicalHistoryRequestDTO();
        request_history.setPatientId(UUID.fromString("22222222-bbbb-2222-bbbb-222222222222"));
        request_history.setAppointmentId(UUID.fromString("33333333-cccc-3333-cccc-333333333333"));
        ConditionRequestDTO request_condition = new ConditionRequestDTO();
        request_condition.setToothNumber(99);
        request_condition.setName("Invalid tooth");
        request_history.setConditions(List.of(request_condition));
        Patient entity_patient = Patient.builder().userId(request_history.getPatientId()).build();
        MedicalHistory entity_history = MedicalHistory.builder()
                .appointmentId(request_history.getAppointmentId())
                .patient(entity_patient)
                .build();
        when(mock_patientRepository.findById(request_history.getPatientId())).thenReturn(java.util.Optional.of(entity_patient));
        when(mock_mapper.toMedicalHistoryEntity(request_history, entity_patient)).thenReturn(entity_history);
        when(mock_medicalHistoryRepository.save(entity_history)).thenReturn(entity_history);

        // --- Act & Assert: expect domain validation before DB write ---
        assertThrows(
                IllegalArgumentException.class,
                () -> medicalHistoryService.createMedicalHistory(request_history)
        );
        verify(mock_medicalHistoryRepository, never()).save(any());

        // --- CheckDB: expected validation should happen before DB write ---
        // --- Rollback: not applicable because no repository write should be attempted ---
    }

    // TC_BusinessLogic_PatientAggregate_DuplicateToothIssue_001
    @Test
    void test_addToothIssue_duplicateToothNumber_shouldRejectDuplicateActiveIssue() {
        /*
         * Test Case ID : TC_BusinessLogic_PatientAggregate_DuplicateToothIssue_001
         * Objective    : Verify desired rule that a patient should not have duplicate active issues for the same tooth.
         * Input        : Add toothNumber=12 twice with status="ACTIVE".
         * Expected     : IllegalStateException on second add.
         */
        // --- Arrange: set up aggregate with one active tooth issue ---
        Patient entity_patient = Patient.builder()
                .userId(UUID.fromString("44444444-dddd-4444-dddd-444444444444"))
                .build();
        entity_patient.addToothIssue(12, "ACTIVE", "First cavity", LocalDate.now(), null);

        // --- Act & Assert: expect duplicate active issue to be rejected ---
        assertThrows(
                IllegalStateException.class,
                () -> entity_patient.addToothIssue(12, "ACTIVE", "Duplicate cavity", LocalDate.now(), null)
        );

        // --- CheckDB: not applicable because aggregate method does not access DB ---
        // --- Rollback: not applicable because no DB state is modified ---
    }
}
