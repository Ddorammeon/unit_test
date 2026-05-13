/*
 * =============================================================================
 * FILE: MedicalHistoryServiceTest.java
 * MODULE UNDER TEST: MedicalHistoryService.java
 * DESCRIPTION: Unit tests for medical history business operations including
 *              create/update, condition synchronization to tooth issues, reads,
 *              deletes, and not-found paths.
 * FRAMEWORK: JUnit 5
 * LIBRARIES: Mockito, Spring Boot Test
 * AUTHOR: Student Team
 * DATE: 2026-05-12
 * =============================================================================
 */

package com.main_project.patient_service.service;

import com.main_project.patient_service.dto.ConditionRequestDTO;
import com.main_project.patient_service.dto.ConditionResponseDTO;
import com.main_project.patient_service.dto.MedicalHistoryRequestDTO;
import com.main_project.patient_service.dto.MedicalHistoryResponseDTO;
import com.main_project.patient_service.entity.Condition;
import com.main_project.patient_service.entity.MedicalHistory;
import com.main_project.patient_service.entity.Patient;
import com.main_project.patient_service.repository.MedicalHistoryRepository;
import com.main_project.patient_service.repository.PatientRepository;
import com.main_project.patient_service.util.EntityMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalHistoryServiceTest {

    @Mock
    private MedicalHistoryRepository mock_medicalHistoryRepository;

    @Mock
    private PatientRepository mock_patientRepository;

    @Mock
    private EntityMapper mock_mapper;

    @InjectMocks
    private MedicalHistoryService medicalHistoryService;

    // TC_MedicalHistoryService_CreateMedicalHistory_001
    @Test
    void test_createMedicalHistory_validRequest_savesHistoryAndCreatesToothIssue() {
        /*
         * Test Case ID : TC_MedicalHistoryService_CreateMedicalHistory_001
         * Objective    : Verify a valid history is saved and condition tooth number is synchronized to patient tooth issue.
         * Input        : request with patientId, appointmentId, symptoms, and one condition toothNumber=14.
         * Expected     : MedicalHistory is saved and patientRepository.save is called for tooth issue sync.
         */
        // --- Arrange: set up valid request, patient, mapper output, and repository save ---
        UUID uuid_patientId = UUID.fromString("11111111-aaaa-1111-aaaa-111111111111");
        UUID uuid_appointmentId = UUID.fromString("22222222-bbbb-2222-bbbb-222222222222");
        UUID uuid_historyId = UUID.fromString("33333333-cccc-3333-cccc-333333333333");
        Patient entity_patient = Patient.builder().userId(uuid_patientId).build();
        MedicalHistoryRequestDTO request_history = buildMedicalHistoryRequest(uuid_patientId, uuid_appointmentId);
        request_history.setConditions(List.of(buildConditionRequest(14, "Cavity", "ACTIVE", "Filling", null)));
        MedicalHistory entity_history = MedicalHistory.builder().id(uuid_historyId).appointmentId(uuid_appointmentId).patient(entity_patient).build();
        MedicalHistoryResponseDTO dto_response = new MedicalHistoryResponseDTO();
        dto_response.setId(uuid_historyId);
        dto_response.setPatientId(uuid_patientId);
        when(mock_patientRepository.findById(uuid_patientId)).thenReturn(Optional.of(entity_patient));
        when(mock_mapper.toMedicalHistoryEntity(request_history, entity_patient)).thenReturn(entity_history);
        when(mock_medicalHistoryRepository.save(entity_history)).thenReturn(entity_history);
        when(mock_mapper.toMedicalHistoryResponse(entity_history)).thenReturn(dto_response);

        // --- Act: call the function under test ---
        MedicalHistoryResponseDTO actual_result = medicalHistoryService.createMedicalHistory(request_history);

        // --- Assert: verify save and tooth issue synchronization ---
        assertEquals(uuid_historyId, actual_result.getId());
        assertEquals(1, entity_patient.getToothIssues().size());
        verify(mock_medicalHistoryRepository).save(entity_history);
        verify(mock_patientRepository).save(entity_patient);

        // --- CheckDB: repository saves are mocked; verify save calls represent DB writes ---
        // --- Rollback: Mockito mocks isolate DB state, so no persistent data is written ---
    }

    // TC_MedicalHistoryService_CreateMedicalHistory_002
    @Test
    void test_createMedicalHistory_missingPatient_throwsEntityNotFoundException() {
        /*
         * Test Case ID : TC_MedicalHistoryService_CreateMedicalHistory_002
         * Objective    : Verify history creation fails when patient does not exist.
         * Input        : request.patientId missing from PatientRepository.
         * Expected     : EntityNotFoundException and no medical history save.
         */
        // --- Arrange: set up request with missing patient id ---
        UUID uuid_missingPatientId = UUID.fromString("44444444-dddd-4444-dddd-444444444444");
        MedicalHistoryRequestDTO request_history = buildMedicalHistoryRequest(
                uuid_missingPatientId,
                UUID.fromString("55555555-eeee-5555-eeee-555555555555")
        );
        when(mock_patientRepository.findById(uuid_missingPatientId)).thenReturn(Optional.empty());

        // --- Act & Assert: verify not-found exception and no write ---
        assertThrows(
                EntityNotFoundException.class,
                () -> medicalHistoryService.createMedicalHistory(request_history)
        );
        verify(mock_medicalHistoryRepository, never()).save(any(MedicalHistory.class));

        // --- CheckDB: repository lookup is mocked; verify no save call means no DB insert ---
        // --- Rollback: not applicable because no repository write is attempted ---
    }

    // TC_MedicalHistoryService_GetMedicalHistoryById_001
    @Test
    void test_getMedicalHistoryById_existingId_returnsResponse() {
        /*
         * Test Case ID : TC_MedicalHistoryService_GetMedicalHistoryById_001
         * Objective    : Verify existing medical history is mapped to response.
         * Input        : id=66666666-6666-6666-6666-666666666666.
         * Expected     : Response DTO with matching id.
         */
        // --- Arrange: set up repository read and mapper output ---
        UUID uuid_historyId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        MedicalHistory entity_history = MedicalHistory.builder().id(uuid_historyId).build();
        MedicalHistoryResponseDTO dto_response = new MedicalHistoryResponseDTO();
        dto_response.setId(uuid_historyId);
        when(mock_medicalHistoryRepository.findById(uuid_historyId)).thenReturn(Optional.of(entity_history));
        when(mock_mapper.toMedicalHistoryResponse(entity_history)).thenReturn(dto_response);

        // --- Act: call the function under test ---
        MedicalHistoryResponseDTO actual_result = medicalHistoryService.getMedicalHistoryById(uuid_historyId);

        // --- Assert: verify mapped response ---
        assertEquals(uuid_historyId, actual_result.getId());
        verify(mock_medicalHistoryRepository).findById(uuid_historyId);

        // --- CheckDB: repository read is mocked; verify findById call represents DB read ---
        // --- Rollback: not applicable because no DB state is modified ---
    }

    // TC_MedicalHistoryService_GetMedicalHistoriesByPatient_001
    @Test
    void test_getMedicalHistoriesByPatient_existingPatient_returnsMappedList() {
        /*
         * Test Case ID : TC_MedicalHistoryService_GetMedicalHistoriesByPatient_001
         * Objective    : Verify patient-specific history lookup maps every result.
         * Input        : patientId=77777777-7777-7777-7777-777777777777; repository returns two rows.
         * Expected     : List size is 2.
         */
        // --- Arrange: set up repository result with two histories ---
        UUID uuid_patientId = UUID.fromString("77777777-7777-7777-7777-777777777777");
        MedicalHistory entity_firstHistory = MedicalHistory.builder().id(UUID.fromString("78787878-7878-7878-7878-787878787878")).build();
        MedicalHistory entity_secondHistory = MedicalHistory.builder().id(UUID.fromString("79797979-7979-7979-7979-797979797979")).build();
        when(mock_medicalHistoryRepository.findByPatient_UserId(uuid_patientId))
                .thenReturn(List.of(entity_firstHistory, entity_secondHistory));
        when(mock_mapper.toMedicalHistoryResponse(entity_firstHistory)).thenReturn(new MedicalHistoryResponseDTO());
        when(mock_mapper.toMedicalHistoryResponse(entity_secondHistory)).thenReturn(new MedicalHistoryResponseDTO());

        // --- Act: call the function under test ---
        List<MedicalHistoryResponseDTO> actual_results =
                medicalHistoryService.getMedicalHistoriesByPatient(uuid_patientId);

        // --- Assert: verify all rows are mapped ---
        assertEquals(2, actual_results.size());
        verify(mock_medicalHistoryRepository).findByPatient_UserId(uuid_patientId);

        // --- CheckDB: repository read is mocked; verify query call represents DB read ---
        // --- Rollback: not applicable because no DB state is modified ---
    }

    // TC_MedicalHistoryService_UpdateMedicalHistory_001
    @Test
    void test_updateMedicalHistory_existingHistory_updatesAndSyncsToothIssue() {
        /*
         * Test Case ID : TC_MedicalHistoryService_UpdateMedicalHistory_001
         * Objective    : Verify update uses mapper, saves history, and syncs condition tooth issue.
         * Input        : existing history id and request with condition toothNumber=21.
         * Expected     : history save and patient save are called.
         */
        // --- Arrange: set up existing history, patient, and update request ---
        UUID uuid_historyId = UUID.fromString("80808080-8080-8080-8080-808080808080");
        UUID uuid_patientId = UUID.fromString("81818181-8181-8181-8181-818181818181");
        Patient entity_patient = Patient.builder().userId(uuid_patientId).build();
        MedicalHistory entity_existingHistory = MedicalHistory.builder().id(uuid_historyId).patient(entity_patient).build();
        MedicalHistoryRequestDTO request_update = buildMedicalHistoryRequest(
                uuid_patientId,
                UUID.fromString("82828282-8282-8282-8282-828282828282")
        );
        request_update.setConditions(List.of(buildConditionRequest(21, "Crack", "ACTIVE", null, "M")));
        when(mock_medicalHistoryRepository.findById(uuid_historyId)).thenReturn(Optional.of(entity_existingHistory));
        when(mock_patientRepository.findById(uuid_patientId)).thenReturn(Optional.of(entity_patient));
        when(mock_medicalHistoryRepository.save(entity_existingHistory)).thenReturn(entity_existingHistory);
        when(mock_mapper.toMedicalHistoryResponse(entity_existingHistory)).thenReturn(new MedicalHistoryResponseDTO());

        // --- Act: call the function under test ---
        medicalHistoryService.updateMedicalHistory(uuid_historyId, request_update);

        // --- Assert: verify update flow and sync save ---
        verify(mock_mapper).updateMedicalHistoryEntity(entity_existingHistory, request_update, entity_patient);
        verify(mock_medicalHistoryRepository).save(entity_existingHistory);
        verify(mock_patientRepository).save(entity_patient);
        assertEquals(1, entity_patient.getToothIssues().size());

        // --- CheckDB: repository saves are mocked; verify save calls represent DB writes ---
        // --- Rollback: Mockito mocks isolate DB state, so no persistent data is written ---
    }

    // TC_MedicalHistoryService_DeleteMedicalHistory_001
    @Test
    void test_deleteMedicalHistory_existingId_deletesById() {
        /*
         * Test Case ID : TC_MedicalHistoryService_DeleteMedicalHistory_001
         * Objective    : Verify existing medical history can be deleted.
         * Input        : id=90909090-9090-9090-9090-909090909090; existsById=true.
         * Expected     : deleteById is called.
         */
        // --- Arrange: set up existing history id ---
        UUID uuid_historyId = UUID.fromString("90909090-9090-9090-9090-909090909090");
        when(mock_medicalHistoryRepository.existsById(uuid_historyId)).thenReturn(true);

        // --- Act: call the function under test ---
        medicalHistoryService.deleteMedicalHistory(uuid_historyId);

        // --- Assert: verify delete call ---
        verify(mock_medicalHistoryRepository).deleteById(uuid_historyId);

        // --- CheckDB: repository delete is mocked; verify deleteById call represents DB delete ---
        // --- Rollback: Mockito mock isolates DB state, so no persistent data is deleted ---
    }

    // TC_MedicalHistoryService_AddConditionToMedicalHistory_001
    @Test
    void test_addConditionToMedicalHistory_existingHistory_addsConditionAndReturnsResponse() {
        /*
         * Test Case ID : TC_MedicalHistoryService_AddConditionToMedicalHistory_001
         * Objective    : Verify a condition is added to an existing medical history.
         * Input        : medicalHistoryId with ConditionRequestDTO(toothNumber=11).
         * Expected     : condition is added, history saved, and condition response returned.
         */
        // --- Arrange: set up existing history, condition entity, and mapper output ---
        UUID uuid_historyId = UUID.fromString("91919191-9191-9191-9191-919191919191");
        UUID uuid_conditionId = UUID.fromString("92929292-9292-9292-9292-929292929292");
        MedicalHistory entity_history = MedicalHistory.builder().id(uuid_historyId).build();
        ConditionRequestDTO request_condition = buildConditionRequest(11, "Pain", "ACTIVE", "Medication", null);
        Condition entity_condition = Condition.builder().id(uuid_conditionId).medicalHistory(entity_history).toothNumber(11).build();
        ConditionResponseDTO dto_response = new ConditionResponseDTO();
        dto_response.setId(uuid_conditionId);
        when(mock_medicalHistoryRepository.findById(uuid_historyId)).thenReturn(Optional.of(entity_history));
        when(mock_mapper.toConditionEntity(request_condition, entity_history)).thenReturn(entity_condition);
        when(mock_medicalHistoryRepository.save(entity_history)).thenReturn(entity_history);
        when(mock_mapper.toConditionResponse(entity_condition)).thenReturn(dto_response);

        // --- Act: call the function under test ---
        ConditionResponseDTO actual_result =
                medicalHistoryService.addConditionToMedicalHistory(uuid_historyId, request_condition);

        // --- Assert: verify condition was added and mapped ---
        assertEquals(uuid_conditionId, actual_result.getId());
        assertEquals(1, entity_history.getConditions().size());
        verify(mock_medicalHistoryRepository).save(entity_history);

        // --- CheckDB: repository save is mocked; verify save call represents DB write ---
        // --- Rollback: Mockito mock isolates DB state, so no persistent data is written ---
    }

    // TC_MedicalHistoryService_UpdateMedicalHistoryByAppointmentId_001
    @Test
    void test_updateMedicalHistoryByAppointmentId_existingAppointment_updatesFirstHistory() {
        /*
         * Test Case ID : TC_MedicalHistoryService_UpdateMedicalHistoryByAppointmentId_001
         * Objective    : Verify appointment-based update selects the first matching history.
         * Input        : appointmentId with one existing history.
         * Expected     : mapper update and repository save are called.
         */
        // --- Arrange: set up appointment lookup and patient ---
        UUID uuid_patientId = UUID.fromString("93939393-9393-9393-9393-939393939393");
        UUID uuid_appointmentId = UUID.fromString("94949494-9494-9494-9494-949494949494");
        Patient entity_patient = Patient.builder().userId(uuid_patientId).build();
        MedicalHistory entity_existingHistory = MedicalHistory.builder().id(UUID.fromString("95959595-9595-9595-9595-959595959595")).build();
        MedicalHistoryRequestDTO request_update = buildMedicalHistoryRequest(uuid_patientId, uuid_appointmentId);
        when(mock_medicalHistoryRepository.findByAppointmentId(uuid_appointmentId)).thenReturn(List.of(entity_existingHistory));
        when(mock_patientRepository.findById(uuid_patientId)).thenReturn(Optional.of(entity_patient));
        when(mock_medicalHistoryRepository.save(entity_existingHistory)).thenReturn(entity_existingHistory);
        when(mock_mapper.toMedicalHistoryResponse(entity_existingHistory)).thenReturn(new MedicalHistoryResponseDTO());

        // --- Act: call the function under test ---
        medicalHistoryService.updateMedicalHistoryByAppointmentId(request_update);

        // --- Assert: verify appointment update flow ---
        verify(mock_mapper).updateMedicalHistoryEntity(entity_existingHistory, request_update, entity_patient);
        verify(mock_medicalHistoryRepository).save(entity_existingHistory);

        // --- CheckDB: repository reads/writes are mocked; verify calls represent DB access ---
        // --- Rollback: Mockito mocks isolate DB state, so no persistent data is written ---
    }

    private MedicalHistoryRequestDTO buildMedicalHistoryRequest(UUID uuid_patientId, UUID uuid_appointmentId) {
        MedicalHistoryRequestDTO request_history = new MedicalHistoryRequestDTO();
        request_history.setPatientId(uuid_patientId);
        request_history.setAppointmentId(uuid_appointmentId);
        request_history.setSymptoms("Tooth pain");
        return request_history;
    }

    private ConditionRequestDTO buildConditionRequest(
            Integer int_toothNumber,
            String str_name,
            String str_status,
            String str_treatment,
            String str_surface
    ) {
        ConditionRequestDTO request_condition = new ConditionRequestDTO();
        request_condition.setToothNumber(int_toothNumber);
        request_condition.setName(str_name);
        request_condition.setStatus(str_status);
        request_condition.setTreatment(str_treatment);
        request_condition.setSurface(str_surface);
        return request_condition;
    }
}
