# OUTPUT BLOCK 1 - Testing Framework & Libraries

- Testing framework: JUnit 5
- Helper/mocking libraries: Mockito, MockitoExtension
- Database testing utilities: None used directly; repositories are mocked for unit tests
- Coverage tools: JaCoCo

# OUTPUT BLOCK 2 - Scope Table: Functions TO TEST

| File / Class | Function / Method | Reason for Testing |
|---|---|---|
| PatientService.java | createPatient(PatientRequestDTO) | Core aggregate creation, allergy lookup, child entity creation, enum conversion |
| PatientService.java | updatePatient(UUID, PatientRequestDTO) | Smart sync business logic for allergies, diseases, and tooth issues |
| PatientService.java | getPatientById(UUID) | Repository read and response mapping |
| PatientService.java | deletePatient(UUID) | Existence check before delete |
| MedicalHistoryService.java | createMedicalHistory(MedicalHistoryRequestDTO) | Patient lookup, history creation, condition-to-tooth issue sync |
| MedicalHistoryService.java | updateMedicalHistory(UUID, MedicalHistoryRequestDTO) | Existing history update and tooth issue sync |
| MedicalHistoryService.java | addConditionToMedicalHistory(UUID, ConditionRequestDTO) | Adds condition to existing medical history |
| MedicalHistoryService.java | updateMedicalHistoryByAppointmentId(MedicalHistoryRequestDTO) | Appointment-based update workflow |
| AllergyService.java | createAllergy(AllergyDTO) | Allergy catalog write operation |
| AllergyService.java | updateAllergy(UUID, AllergyDTO) | Catalog update and not-found behavior |
| AllergyService.java | getAllergyById(UUID), getAllAllergies() | Catalog read behavior |
| Patient.java | aggregate child methods | Business relationship and back-reference behavior |
| EntityMapper.java | mapping methods | DTO/entity conversion used by service layer |
| BusinessLogicFailingTest.java | opt-in business expectation tests | Documents missing service-level validation rules |

# OUTPUT BLOCK 3 - Scope Table: Functions NOT TO TEST

| File / Class | Function / Method | Reason NOT Tested |
|---|---|---|
| PatientServiceApplication.java | main(String[]) | Spring Boot entry point; metadata smoke test only |
| SecurityConfig.java | (entire file) | Configuration only, no custom business branching tested here |
| AxonConfig.java | (entire file) | Infrastructure configuration |
| Repository interfaces | Spring Data generated methods | Behavior owned by Spring Data JPA |
| DTO getters/setters | Lombok-generated accessors | Trivial generated code |
| GlobalExceptionHandler.java | HTTP exception mapping | Controller integration concern, outside service-focused unit test scope |

# OUTPUT BLOCK 4 - Test Case Tables

## PatientServiceTest

| Test Case ID | Test Objective | Input | Expected Output | Notes |
|---|---|---|---|---|
| TC_PatientService_CreatePatient_001 | Create patient with all child collections | Valid request with allergy, disease, tooth issue | Saved aggregate and response contains children | Mock repositories; CheckDB/Rollback via mock verification |
| TC_PatientService_CreatePatient_002 | Missing allergy master fails | Allergy id not found | EntityNotFoundException, no save | Business rule dependency |
| TC_PatientService_UpdatePatient_001 | Replace provided child collections | Existing old children, request has new lists | Response contains only new values | Smart sync |
| TC_PatientService_UpdatePatient_002 | Null child collections keep existing data | child lists null | Existing children preserved | Boundary behavior |
| TC_PatientService_GetPatientById_001 | Read patient by user id | Existing user id | PatientResponseDTO | DB read mocked |
| TC_PatientService_DeletePatient_001 | Delete existing patient | existsById=true | deleteById called | DB write mocked |
| TC_PatientService_CreatePatient_003 | Reject invalid gender | gender="INVALID" | IllegalArgumentException | Business value validation |
| TC_PatientService_CreatePatient_004 | Minimal valid patient | Empty child lists | Saves with zero children | Boundary empty lists |

## MedicalHistoryServiceTest

| Test Case ID | Test Objective | Input | Expected Output | Notes |
|---|---|---|---|---|
| TC_MedicalHistoryService_CreateMedicalHistory_001 | Create history and sync tooth issue | Condition toothNumber=14 | history saved, patient saved | DB writes mocked |
| TC_MedicalHistoryService_CreateMedicalHistory_002 | Missing patient fails | patientId missing | EntityNotFoundException | no save |
| TC_MedicalHistoryService_GetMedicalHistoryById_001 | Read history by id | Existing history id | Response DTO | DB read mocked |
| TC_MedicalHistoryService_GetMedicalHistoriesByPatient_001 | Read by patient | Two histories | Two DTOs | DB read mocked |
| TC_MedicalHistoryService_UpdateMedicalHistory_001 | Update and sync tooth issue | condition toothNumber=21 | history and patient saved | Business sync |
| TC_MedicalHistoryService_DeleteMedicalHistory_001 | Delete existing history | existsById=true | deleteById called | DB delete mocked |
| TC_MedicalHistoryService_AddConditionToMedicalHistory_001 | Add condition | Condition toothNumber=11 | condition added and response returned | DB write mocked |
| TC_MedicalHistoryService_UpdateMedicalHistoryByAppointmentId_001 | Update first history by appointment | One matching appointment history | mapper update and save called | DB read/write mocked |

## AllergyServiceTest

| Test Case ID | Test Objective | Input | Expected Output | Notes |
|---|---|---|---|---|
| TC_AllergyService_CreateAllergy_001 | Create allergy | name="Penicillin" | Saved DTO | DB write mocked |
| TC_AllergyService_UpdateAllergy_001 | Update allergy | Existing id, new fields | Updated DTO | DB write mocked |
| TC_AllergyService_UpdateAllergy_002 | Missing allergy update fails | Missing id | EntityNotFoundException | no save |
| TC_AllergyService_GetAllergyById_001 | Read by id | Existing id | DTO | DB read mocked |
| TC_AllergyService_GetAllAllergies_001 | Read all | Two entities | Two DTOs | DB read mocked |

## BusinessLogicFailingTest

| Test Case ID | Test Objective | Input | Expected Output | Notes |
|---|---|---|---|---|
| TC_BusinessLogic_PatientService_FutureDob_001 | Document DOB must be in the past at service layer | dob=tomorrow | Expected IllegalArgumentException; currently no exception | Opt-in fail |
| TC_BusinessLogic_AllergyService_BlankName_001 | Document allergy name required at service layer | name="" | Expected IllegalArgumentException; currently no exception | Opt-in fail |
| TC_BusinessLogic_MedicalHistoryService_InvalidToothNumber_001 | Document tooth number must be 1-32 | toothNumber=99 | Expected IllegalArgumentException; currently no exception | Opt-in fail |
| TC_BusinessLogic_PatientAggregate_DuplicateToothIssue_001 | Document no duplicate active issue for same tooth | add tooth 12 ACTIVE twice | Expected IllegalStateException; currently no exception | Opt-in fail |

# OUTPUT BLOCK 5 - Test Scripts

- `src/test/java/com/main_project/patient_service/service/PatientServiceTest.java`
- `src/test/java/com/main_project/patient_service/service/MedicalHistoryServiceTest.java`
- `src/test/java/com/main_project/patient_service/service/AllergyServiceTest.java`
- `src/test/java/com/main_project/patient_service/util/EntityMapperTest.java`
- `src/test/java/com/main_project/patient_service/entity/PatientAggregateTest.java`
- `src/test/java/com/main_project/patient_service/BusinessLogicFailingTest.java`
- `src/test/java/com/main_project/patient_service/PatientServiceApplicationTests.java`

# OUTPUT BLOCK 5.1 - Coverage And Commands

- Pass command: `./gradlew test --rerun-tasks`
- Business-fail command: `./gradlew test -DincludeBusinessLogicFailures=true --rerun-tasks`
- HTML coverage: `build/reports/jacoco/test/html/index.html`
- SVG coverage image: `build/reports/jacoco/test/coverage-summary.svg`
- Current line coverage from SVG: 53% (514 covered lines, 464 missed lines)
- JUnit report: `build/reports/tests/test/index.html`

# OUTPUT BLOCK 6 - References & Prompts Used

1. Prompt: "f:\\dbclpm\\ThietKeVaTrienKhaiHeThongNhaKhoaThongMinhTichHopChatBotAI\\service\\patient-service h viết cho tôi test cho service này vẫn theo luật ở file nàyf:\\dbclpm\\ThietKeVaTrienKhaiHeThongNhaKhoaThongMinhTichHopChatBotAI\\rule.md viết test case nhiều 1 chút, test chủ yếu nghiệp vụ thôi, test chi tiết để biết có test case fail ở bussiness logic ấy,"
   Purpose: Generate service-focused JUnit/Mockito unit tests and opt-in business-logic failing test cases for patient-service.
