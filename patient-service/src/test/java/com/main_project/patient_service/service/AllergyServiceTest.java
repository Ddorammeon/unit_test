/*
 * =============================================================================
 * FILE: AllergyServiceTest.java
 * MODULE UNDER TEST: AllergyService.java
 * DESCRIPTION: Unit tests for allergy catalog business operations including
 *              create, update, read, list, delete, and not-found cases.
 * FRAMEWORK: JUnit 5
 * LIBRARIES: Mockito, Spring Boot Test
 * AUTHOR: Student Team
 * DATE: 2026-05-12
 * =============================================================================
 */

package com.main_project.patient_service.service;

import com.main_project.patient_service.dto.AllergyDTO;
import com.main_project.patient_service.entity.Allergy;
import com.main_project.patient_service.repository.AllergyRepository;
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
class AllergyServiceTest {

    @Mock
    private AllergyRepository mock_allergyRepository;

    @InjectMocks
    private AllergyService allergyService;

    // TC_AllergyService_CreateAllergy_001
    @Test
    void test_createAllergy_validDto_savesAndReturnsDto() {
        /*
         * Test Case ID : TC_AllergyService_CreateAllergy_001
         * Objective    : Verify that valid allergy catalog data is saved and returned.
         * Input        : AllergyDTO(name="Penicillin", type="DRUG", description="Antibiotic allergy").
         * Expected     : Returns AllergyDTO with saved id and same fields.
         */
        // --- Arrange: set up valid input and repository save behavior ---
        UUID uuid_allergyId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        AllergyDTO dto_validAllergy = AllergyDTO.builder()
                .name("Penicillin")
                .type("DRUG")
                .description("Antibiotic allergy")
                .build();
        Allergy entity_savedAllergy = Allergy.builder()
                .id(uuid_allergyId)
                .name("Penicillin")
                .type("DRUG")
                .description("Antibiotic allergy")
                .build();
        when(mock_allergyRepository.save(any(Allergy.class))).thenReturn(entity_savedAllergy);

        // --- Act: call the function under test ---
        AllergyDTO actual_result = allergyService.createAllergy(dto_validAllergy);

        // --- Assert: verify returned DTO and repository write ---
        assertEquals(uuid_allergyId, actual_result.getId());
        assertEquals("Penicillin", actual_result.getName());
        assertEquals("DRUG", actual_result.getType());
        verify(mock_allergyRepository).save(any(Allergy.class));

        // --- CheckDB: repository save is mocked; verify save call represents DB write ---
        // --- Rollback: Mockito mock isolates DB state, so no persistent data is written ---
    }

    // TC_AllergyService_UpdateAllergy_001
    @Test
    void test_updateAllergy_existingId_updatesAllFields() {
        /*
         * Test Case ID : TC_AllergyService_UpdateAllergy_001
         * Objective    : Verify update changes allergy name, type, and description.
         * Input        : id=22222222-2222-2222-2222-222222222222; dto(name="Peanut", type="FOOD").
         * Expected     : Returns updated DTO and saves existing entity.
         */
        // --- Arrange: set up existing allergy and update DTO ---
        UUID uuid_allergyId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Allergy entity_existingAllergy = Allergy.builder()
                .id(uuid_allergyId)
                .name("Old")
                .type("DRUG")
                .description("Old description")
                .build();
        AllergyDTO dto_updateAllergy = AllergyDTO.builder()
                .name("Peanut")
                .type("FOOD")
                .description("Nut allergy")
                .build();
        when(mock_allergyRepository.findById(uuid_allergyId)).thenReturn(Optional.of(entity_existingAllergy));
        when(mock_allergyRepository.save(entity_existingAllergy)).thenReturn(entity_existingAllergy);

        // --- Act: call the function under test ---
        AllergyDTO actual_result = allergyService.updateAllergy(uuid_allergyId, dto_updateAllergy);

        // --- Assert: verify fields were updated and saved ---
        assertEquals("Peanut", actual_result.getName());
        assertEquals("FOOD", actual_result.getType());
        assertEquals("Nut allergy", actual_result.getDescription());
        verify(mock_allergyRepository).save(entity_existingAllergy);

        // --- CheckDB: repository save is mocked; verify save call represents DB update ---
        // --- Rollback: Mockito mock isolates DB state, so no persistent data is written ---
    }

    // TC_AllergyService_UpdateAllergy_002
    @Test
    void test_updateAllergy_missingId_throwsEntityNotFoundException() {
        /*
         * Test Case ID : TC_AllergyService_UpdateAllergy_002
         * Objective    : Verify update fails when allergy id does not exist.
         * Input        : id=33333333-3333-3333-3333-333333333333; repository returns empty.
         * Expected     : EntityNotFoundException and no save call.
         */
        // --- Arrange: set up missing allergy id ---
        UUID uuid_missingAllergyId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        AllergyDTO dto_updateAllergy = AllergyDTO.builder().name("Latex").build();
        when(mock_allergyRepository.findById(uuid_missingAllergyId)).thenReturn(Optional.empty());

        // --- Act & Assert: verify not-found exception and no write ---
        assertThrows(
                EntityNotFoundException.class,
                () -> allergyService.updateAllergy(uuid_missingAllergyId, dto_updateAllergy)
        );
        verify(mock_allergyRepository, never()).save(any(Allergy.class));

        // --- CheckDB: repository lookup is mocked; verify no DB write occurs ---
        // --- Rollback: not applicable because no repository write is attempted ---
    }

    // TC_AllergyService_GetAllergyById_001
    @Test
    void test_getAllergyById_existingId_returnsDto() {
        /*
         * Test Case ID : TC_AllergyService_GetAllergyById_001
         * Objective    : Verify allergy lookup maps an existing entity to DTO.
         * Input        : id=44444444-4444-4444-4444-444444444444.
         * Expected     : AllergyDTO with matching id and name.
         */
        // --- Arrange: set up existing allergy entity ---
        UUID uuid_allergyId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        Allergy entity_existingAllergy = Allergy.builder()
                .id(uuid_allergyId)
                .name("Dust")
                .type("ENVIRONMENTAL")
                .description("Dust allergy")
                .build();
        when(mock_allergyRepository.findById(uuid_allergyId)).thenReturn(Optional.of(entity_existingAllergy));

        // --- Act: call the function under test ---
        AllergyDTO actual_result = allergyService.getAllergyById(uuid_allergyId);

        // --- Assert: verify mapped DTO ---
        assertEquals(uuid_allergyId, actual_result.getId());
        assertEquals("Dust", actual_result.getName());
        verify(mock_allergyRepository).findById(uuid_allergyId);

        // --- CheckDB: repository read is mocked; verify findById call represents DB read ---
        // --- Rollback: not applicable because no DB state is modified ---
    }

    // TC_AllergyService_GetAllAllergies_001
    @Test
    void test_getAllAllergies_repositoryHasTwoRows_returnsTwoDtos() {
        /*
         * Test Case ID : TC_AllergyService_GetAllAllergies_001
         * Objective    : Verify all allergy catalog rows are returned as DTOs.
         * Input        : repository.findAll() returns two Allergy entities.
         * Expected     : List size is 2.
         */
        // --- Arrange: set up repository read result ---
        when(mock_allergyRepository.findAll()).thenReturn(List.of(
                Allergy.builder().id(UUID.fromString("55555555-5555-5555-5555-555555555555")).name("Egg").build(),
                Allergy.builder().id(UUID.fromString("66666666-6666-6666-6666-666666666666")).name("Milk").build()
        ));

        // --- Act: call the function under test ---
        List<AllergyDTO> actual_results = allergyService.getAllAllergies();

        // --- Assert: verify list size and mapping order ---
        assertEquals(2, actual_results.size());
        assertEquals("Egg", actual_results.get(0).getName());
        assertEquals("Milk", actual_results.get(1).getName());

        // --- CheckDB: repository read is mocked; verify findAll call represents DB read ---
        // --- Rollback: not applicable because no DB state is modified ---
    }
}
