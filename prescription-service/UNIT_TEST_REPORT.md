# OUTPUT BLOCK 1 - Testing Framework & Libraries

- Testing framework: JUnit 5
- Helper/mocking libraries: Mockito, MockitoExtension
- Database testing utilities: None used; tested units do not access DB directly
- Coverage tools: None configured in `build.gradle` (JaCoCo not present)

# OUTPUT BLOCK 2 - Scope Table: Functions TO TEST

| File / Class | Function / Method | Reason for Testing |
|---|---|---|
| PrescriptionService.java | getPrescriptionStatus(UUID) | Delegates prescription status lookup to InventoryClient and must propagate dependency output/failure correctly |
| PrescriptionBillingController.java | createPrescription(CreatePrescriptionRequest) | User-facing command entry point; builds and sends CreatePrescriptionCommand |
| PrescriptionBillingController.java | getStatus(UUID) | User-facing read endpoint; delegates to PrescriptionService and wraps response |
| PrescriptionCommandHandler.java | handle(CreatePrescriptionCommand) | Core business rule validation, invoice dependency read, mapper call, and aggregate creation |
| PrescriptionBillingServiceApplication.java | class metadata | Smoke check for required Spring Boot and Feign annotations without loading DB-backed context |
| InvoiceItemMapper.java | toCheckerRequest(InvoiceItemResponseDTO) | Verifies MapStruct field mapping and null handling |
| InvoiceItemMapper.java | toCheckerRequests(List<InvoiceItemResponseDTO>) | Verifies list mapping and empty list boundary behavior |
| BusinessLogicFailingTest.java | test_getPrescriptionStatus_nullMedicalHistoryId_shouldRejectBeforeCallingInventory() | Opt-in failing test that documents missing required-id validation |
| BusinessLogicFailingTest.java | test_createPrescription_emptyMedicineItems_shouldReturnBadRequestAndNotDispatchCommand() | Opt-in failing test that documents missing controller validation for empty prescription |
| BusinessLogicFailingTest.java | test_handle_emptyInvoiceList_shouldThrowBusinessExceptionMessage() | Opt-in failing test that documents missing domain error for appointment without invoice |

# OUTPUT BLOCK 3 - Scope Table: Functions NOT TO TEST

| File / Class | Function / Method | Reason NOT Tested |
|---|---|---|
| PrescriptionBillingServiceApplication.java | main(String[]) | Spring Boot entry point; invoking it would load external infrastructure and is covered indirectly by metadata smoke test |
| AxonConfig.java | (entire file) | Configuration only, no custom branching logic |
| InventoryClient.java | (interface methods) | Feign client interface; behavior owned by Spring Cloud OpenFeign |
| InvoiceClient.java | (interface methods) | Feign client interface; behavior owned by Spring Cloud OpenFeign |
| CreatePrescriptionRequest.java | getters/setters | Lombok-generated trivial accessors |
| InvoiceResponseDTO.java | getters/setters | Lombok-generated trivial accessors |
| InvoiceItemResponseDTO.java | getters/setters | Lombok-generated trivial accessors |

# OUTPUT BLOCK 4 - Test Case Tables

## PrescriptionServiceTest

| Test Case ID | Test Objective | Input | Expected Output | Notes |
|---|---|---|---|---|
| TC_PrescriptionService_GetPrescriptionStatus_001 | Verify status is returned for a valid medical history id | `medicalHistoryId=11111111-1111-1111-1111-111111111111`; InventoryClient returns `{status=RELEASED, dispenseOrderId=DO-001}` | Exact same map is returned | Dependency Behavior covered; DB categories skipped because no DB access |
| TC_PrescriptionService_GetPrescriptionStatus_002 | Verify empty dependency response is propagated | `medicalHistoryId=22222222-2222-2222-2222-222222222222`; InventoryClient returns empty map | Exact empty map is returned | Null/Empty covered; DB categories skipped because no DB access |
| TC_PrescriptionService_GetPrescriptionStatus_003 | Verify current null input behavior | `medicalHistoryId=null`; InventoryClient returns `{status=NONE}` | Exact same map is returned | Null covered; no validation exists in source |
| TC_PrescriptionService_GetPrescriptionStatus_004 | Verify dependency exception behavior | `medicalHistoryId=33333333-3333-3333-3333-333333333333`; InventoryClient throws `IllegalStateException` | Same exception is propagated | Failure-path test |
| TC_PrescriptionService_GetPrescriptionStatus_005 | Verify null dependency response behavior | `medicalHistoryId=44444444-4444-4444-4444-444444444444`; InventoryClient returns null | Returns null | Boundary behavior covered |

## PrescriptionBillingControllerTest

| Test Case ID | Test Objective | Input | Expected Output | Notes |
|---|---|---|---|---|
| TC_PrescriptionBillingController_CreatePrescription_001 | Verify valid request dispatches command | Request includes appointment, patient, doctor, medical history, one medicine item | HTTP 202; body has id/message; CommandGateway receives matching command | Happy Path and Dependency Behavior covered |
| TC_PrescriptionBillingController_GetStatus_001 | Verify status endpoint wraps service result | `medicalHistoryId=99999999-9999-9999-9999-999999999999`; service returns `{status=SOLD}` | HTTP 200 and exact map body | Read endpoint covered without DB because service is mocked |
| TC_PrescriptionBillingController_CreatePrescription_002 | Verify command gateway failure path | Valid request; CommandGateway throws `IllegalStateException` | Same exception is propagated | Failure-path test |
| TC_PrescriptionBillingController_CreatePrescription_003 | Verify empty medicine list boundary at controller | Valid request with `items=[]` | HTTP 202; command contains empty items | Validation belongs to command handler in current source |
| TC_PrescriptionBillingController_CreatePrescription_004 | Verify null request failure path | `request=null` | Throws `NullPointerException`; command gateway not called | Failure-path test |
| TC_PrescriptionBillingController_GetStatus_002 | Verify status service failure path | Service throws `IllegalStateException` | Same exception is propagated | Failure-path test |

## PrescriptionCommandHandlerTest

| Test Case ID | Test Objective | Input | Expected Output | Notes |
|---|---|---|---|---|
| TC_PrescriptionCommandHandler_Handle_001 | Verify valid command creates aggregate | Command with one medicine item; invoice client returns one invoice; mapper returns one service item | InvoiceClient and mapper called; repository creates aggregate | Happy Path and Dependency Behavior covered |
| TC_PrescriptionCommandHandler_Handle_002 | Verify empty medicine list is rejected | `CreatePrescriptionCommand.items=[]` | Throws `IllegalStateException("Không có thuốc để tạo đơn thuốc")`; no invoice/repository call | Null/Empty and Business Rule Violation covered |
| TC_PrescriptionCommandHandler_Handle_003 | Verify null medicine list is rejected | `CreatePrescriptionCommand.items=null` | Throws `IllegalStateException("Không có thuốc để tạo đơn thuốc")`; no invoice/repository call | Null covered |
| TC_PrescriptionCommandHandler_Handle_004 | Verify invoice dependency failure path | Valid command; InvoiceClient throws `IllegalStateException` | Same exception is propagated; repository not called | Failure-path test |
| TC_PrescriptionCommandHandler_Handle_005 | Verify repository failure wrapping | Valid command; Repository throws checked `Exception` | Throws `RuntimeException` whose cause is original exception | Failure-path test |
| TC_PrescriptionCommandHandler_Handle_006 | Verify empty invoice list failure path | Valid command; InvoiceClient returns `[]` | Throws `IndexOutOfBoundsException`; repository not called | Failure-path test |
| TC_PrescriptionCommandHandler_Handle_007 | Verify mapper failure path | Valid command and invoice; mapper throws `IllegalArgumentException` | Same exception is propagated; repository not called | Failure-path test |

## InvoiceItemMapperTest

| Test Case ID | Test Objective | Input | Expected Output | Notes |
|---|---|---|---|---|
| TC_InvoiceItemMapper_ToCheckerRequest_001 | Verify single DTO mapping | DTO with id, referenceId, serviceType, quantity, description, unitPrice | Checker request has identical matching values | MapStruct unit test |
| TC_InvoiceItemMapper_ToCheckerRequests_001 | Verify list mapping | List with one DTO | New list with one mapped checker request | Happy path |
| TC_InvoiceItemMapper_ToCheckerRequests_002 | Verify empty list mapping | `dtoList=[]` | Empty list | Boundary test |
| TC_InvoiceItemMapper_ToCheckerRequest_002 | Verify null DTO mapping | `dto=null` | Returns null | Null test |

## BusinessLogicFailingTest

| Test Case ID | Test Objective | Input | Expected Output | Notes |
|---|---|---|---|---|
| TC_BusinessLogic_PrescriptionService_NullMedicalHistoryId_001 | Document desired rule that medicalHistoryId is required | `medicalHistoryId=null` | Expected `IllegalArgumentException`; currently fails because service delegates null to InventoryClient | Opt-in only: `-DincludeBusinessLogicFailures=true` |
| TC_BusinessLogic_Controller_EmptyMedicineItems_001 | Document desired rule that prescription request must contain at least one medicine | Request with `items=[]` | Expected HTTP 400; currently fails because controller returns HTTP 202 and dispatches command | Opt-in only: `-DincludeBusinessLogicFailures=true` |
| TC_BusinessLogic_CommandHandler_NoInvoice_001 | Document desired rule that appointment must have invoice before creating prescription aggregate | Valid command; InvoiceClient returns `[]` | Expected `IllegalStateException("Không tìm thấy hóa đơn cho lịch hẹn")`; currently fails with `ArrayIndexOutOfBoundsException` | Opt-in only: `-DincludeBusinessLogicFailures=true` |

## PrescriptionBillingServiceApplicationTests

| Test Case ID | Test Objective | Input | Expected Output | Notes |
|---|---|---|---|---|
| TC_PrescriptionBillingServiceApplication_ClassMetadata_001 | Verify application class keeps required framework annotations | `PrescriptionBillingServiceApplication.class` metadata | `@SpringBootApplication` and `@EnableFeignClients` are present | Replaces DB-backed context test with unit-level smoke test |

# OUTPUT BLOCK 5 - Test Scripts

- `src/test/java/com/do_an/prescriptionbillingservice/service/PrescriptionServiceTest.java`
- `src/test/java/com/do_an/prescriptionbillingservice/controller/PrescriptionBillingControllerTest.java`
- `src/test/java/com/do_an/prescriptionbillingservice/aggregate/PrescriptionCommandHandlerTest.java`
- `src/test/java/com/do_an/prescriptionbillingservice/PrescriptionBillingServiceApplicationTests.java`
- `src/test/java/com/do_an/prescriptionbillingservice/util/InvoiceItemMapperTest.java`
- `src/test/java/com/do_an/prescriptionbillingservice/BusinessLogicFailingTest.java`

# OUTPUT BLOCK 5.1 - Coverage Reports

- HTML report: `build/reports/jacoco/test/html/index.html`
- SVG coverage image: `build/reports/jacoco/test/coverage-summary.svg`
- Current line coverage from generated SVG: 30% (59 covered lines, 140 missed lines)
- Business-logic failing test command: `./gradlew test -DincludeBusinessLogicFailures=true --rerun-tasks`
- Failing test report after opt-in run: `build/reports/tests/test/index.html`

# OUTPUT BLOCK 6 - References & Prompts Used

1. Prompt: "f:\\dbclpm\\ThietKeVaTrienKhaiHeThongNhaKhoaThongMinhTichHopChatBotAI\\service\\prescription-service viết test cho service này dùng junit và mockito, viết test theo luật ở filef:\\dbclpm\\ThietKeVaTrienKhaiHeThongNhaKhoaThongMinhTichHopChatBotAI\\rule.md này, viết thêm có test fail nữa đi"
   Purpose: Generate JUnit/Mockito tests and rule-compliant test specification for prescription-service.
