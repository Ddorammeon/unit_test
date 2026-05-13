# Unit Testing Report - doctor-service

## 1.1. Tools and Libraries
- Testing framework: JUnit 5 (`org.junit.jupiter`)
- Mocking library: Mockito (`org.mockito`)
- Assertions: AssertJ (`org.assertj`)
- Coverage tool: JaCoCo
- Build tool: Gradle

## 1.2. Scope of Testing
### Files/classes tested
- `src/main/java/com/main_project/doctor_service/service/DoctorService.java`
- `src/main/java/com/main_project/doctor_service/service/DoctorDegreeService.java`
- `src/main/java/com/main_project/doctor_service/service/DoctorWorkScheduleService.java`
- `src/main/java/com/main_project/doctor_service/service/WorkScheduleService.java`
- `src/main/java/com/main_project/doctor_service/entity/Doctor.java`
- `src/main/java/com/main_project/doctor_service/entity/DoctorDegree.java`
- `src/main/java/com/main_project/doctor_service/entity/DoctorWorkSchedule.java`
- `src/main/java/com/main_project/doctor_service/entity/WorkSchedule.java`
- `src/main/java/com/main_project/doctor_service/util/EntityMapper.java`

### Files/classes not covered by unit tests
- `controller/*`: primarily HTTP routing and validation wiring; better suited to controller/integration tests.
- `repository/*`: Spring Data generated behavior should be validated with integration tests against a real DB, not Mockito-only unit tests.
- `entity/*`, `dto/*`, `enums/*`: mainly data carriers with minimal/no business logic.
- `configuration/*`, `exceptions/*`: framework configuration and global exception plumbing are better verified via Spring integration tests.
- `DoctorServiceApplication.java`: bootstrap entrypoint; low-value for unit testing.

### CheckDB and Rollback statement
- The implemented test suite is pure unit test using Mockito and does not connect to PostgreSQL.
- Because no real database access is performed, DB state is not modified.
- `CheckDB` is satisfied by verifying repository method calls (`save`, `findById`, `existsById`, `deleteById`) expected by each service method.
- `Rollback` is inherently satisfied because there is no real DB transaction/data mutation.

## 1.3. Unit test cases
### DoctorServiceTest
| Test Case ID | Test Objective | Input | Expected Output | Notes |
|---|---|---|---|---|
| DOC-SRV-UT-001 | Create doctor successfully | Valid `DoctorRequestDTO`, `existsById=false` | Returns mapped `DoctorResponseDTO`, repository `save` called once | Happy path |
| DOC-SRV-UT-002 | Reject duplicate doctor | Valid `DoctorRequestDTO`, `existsById=true` | Throw `DataIntegrityViolationException` | Duplicate ID |
| DOC-SRV-UT-003 | Get all doctors | Repository returns 2 doctors | Returns 2 mapped DTOs | Read-only flow |
| DOC-SRV-UT-004 | Get doctor by ID successfully | Existing `userId` | Returns mapped DTO | Happy path |
| DOC-SRV-UT-005 | Get doctor by ID not found | Missing `userId` | Throw `EntityNotFoundException` | Negative path |
| DOC-SRV-UT-006 | Update doctor successfully | Existing `userId`, valid DTO | Mapper updates entity, repository saves, returns DTO | Happy path |
| DOC-SRV-UT-007 | Update doctor not found | Missing `userId` | Throw `EntityNotFoundException` | Negative path |
| DOC-SRV-UT-008 | Delete doctor successfully | Existing `userId` | `deleteById` called once | Check repository interaction |
| DOC-SRV-UT-009 | Delete doctor not found | Missing `userId` | Throw `EntityNotFoundException` | Negative path |

### DoctorEntityTest
| Test Case ID | Test Objective | Input | Expected Output | Notes |
|---|---|---|---|---|
| DOC-ENT-UT-001 | Add degree to doctor | Empty doctor aggregate + degree data | New degree added and linked back to doctor | Aggregate logic |
| DOC-ENT-UT-002 | Remove degree from doctor | Doctor with one degree | Degree removed and `doctor` ref cleared | Aggregate consistency |
| DOC-ENT-UT-003 | Remove degree by existing ID | Doctor with known degree ID | Returns `true`, degree removed | Business rule |
| DOC-ENT-UT-004 | Remove degree by missing ID | Doctor without matching degree ID | Returns `false`, list unchanged | Negative path |
| DOC-ENT-UT-005 | Replace all degrees | Doctor with old degrees + new degree list | Old degrees removed, new list added | Aggregate replacement |
| DOC-ENT-UT-006 | Clear degrees by null replacement | Doctor with existing degrees + `null` replacement list | Degree list becomes empty | Null-handling rule |
| DOC-ENT-UT-007 | Clear all degrees explicitly | Doctor with 2 degrees | Degree list becomes empty | Aggregate cleanup |
| DOC-ENT-UT-008 | Update doctor basic info | Existing doctor + new specialization/hospital/license/fee | All business fields updated | Aggregate update logic |

### DoctorDegreeServiceTest
| Test Case ID | Test Objective | Input | Expected Output | Notes |
|---|---|---|---|---|
| DOC-DEG-UT-001 | Create degree successfully | Valid DTO, existing doctor | Saved degree mapped to response DTO | Deprecated service but still testable |
| DOC-DEG-UT-002 | Reject create when doctor missing | Valid DTO, missing doctor | Throw `EntityNotFoundException` | Negative path |
| DOC-DEG-UT-003 | Get all degrees | Repository returns 2 degrees | Returns mapped DTO list | Read-only flow |
| DOC-DEG-UT-004 | Get degree by ID successfully | Existing degree ID | Returns mapped DTO | Happy path |
| DOC-DEG-UT-005 | Get degree by ID not found | Missing degree ID | Throw `EntityNotFoundException` | Negative path |
| DOC-DEG-UT-006 | Get degrees by doctor ID | Existing doctor ID | Returns mapped DTO list | Query flow |
| DOC-DEG-UT-007 | Update degree successfully | Existing degree + existing doctor | Updated entity saved and mapped | Happy path |
| DOC-DEG-UT-008 | Reject update when degree missing | Missing degree ID | Throw `EntityNotFoundException` | Negative path |
| DOC-DEG-UT-009 | Delete degree successfully | Existing degree ID | `deleteById` called once | Check repository interaction |
| DOC-DEG-UT-010 | Delete degree not found | Missing degree ID | Throw `EntityNotFoundException` | Negative path |

### DoctorWorkScheduleServiceTest
| Test Case ID | Test Objective | Input | Expected Output | Notes |
|---|---|---|---|---|
| DOC-WS-UT-001 | Create doctor-work-schedule successfully | Valid DTO, existing doctor and work schedule | Saved entity mapped to response DTO | Happy path |
| DOC-WS-UT-002 | Reject create when doctor missing | Valid DTO, missing doctor | Throw `EntityNotFoundException` | Negative path |
| DOC-WS-UT-003 | Get all doctor-work-schedules | Repository returns 2 records | Returns mapped DTO list | Read-only flow |
| DOC-WS-UT-004 | Get record by ID successfully | Existing ID | Returns mapped DTO | Happy path |
| DOC-WS-UT-005 | Get record by ID not found | Missing ID | Throw `EntityNotFoundException` | Negative path |
| DOC-WS-UT-006 | Get records by doctor ID | Existing doctor ID | Returns mapped DTO list | Query flow |
| DOC-WS-UT-007 | Update record successfully | Existing link ID + existing doctor + existing schedule | Mapper updates entity, repository saves, returns DTO | Happy path |
| DOC-WS-UT-008 | Delete record successfully | Existing ID | `deleteById` called once | Check repository interaction |
| DOC-WS-UT-009 | Delete record not found | Missing ID | Throw `EntityNotFoundException` | Negative path |

### WorkScheduleServiceTest
| Test Case ID | Test Objective | Input | Expected Output | Notes |
|---|---|---|---|---|
| WORK-SRV-UT-001 | Create work schedule successfully | Valid `WorkScheduleRequestDTO` | Returns mapped response DTO | Happy path |
| WORK-SRV-UT-002 | Get all work schedules | Repository returns 2 records | Returns mapped DTO list | Read-only flow |
| WORK-SRV-UT-003 | Get work schedule by ID successfully | Existing ID | Returns mapped DTO | Happy path |
| WORK-SRV-UT-004 | Get work schedule by ID not found | Missing ID | Throw `EntityNotFoundException` | Negative path |
| WORK-SRV-UT-005 | Update work schedule successfully | Existing ID + valid DTO | Mapper updates entity, repository saves, returns DTO | Happy path |
| WORK-SRV-UT-006 | Update work schedule not found | Missing ID | Throw `EntityNotFoundException` | Negative path |
| WORK-SRV-UT-007 | Delete work schedule successfully | Existing ID | `deleteById` called once | Check repository interaction |
| WORK-SRV-UT-008 | Delete work schedule not found | Missing ID | Throw `EntityNotFoundException` | Negative path |

### EntityMapperTest
| Test Case ID | Test Objective | Input | Expected Output | Notes |
|---|---|---|---|---|
| MAP-UT-001 | Map doctor request to entity | Doctor request with 1 degree | Doctor entity created with nested degree | Mapping logic |
| MAP-UT-002 | Update doctor entity with replacement data | Existing doctor + request with new fields/degrees | Fields updated, old degrees replaced | Business mapping |
| MAP-UT-003 | Clear doctor degrees when request degrees is null | Existing doctor + request with `degrees=null` | Degrees cleared | Null-handling logic |
| MAP-UT-004 | Map doctor entity to response | Doctor entity with 1 degree | Response DTO includes nested degree DTO | Mapping logic |
| MAP-UT-005 | Map work schedule request to entity | Valid schedule request | Work schedule entity fields populated | Mapping logic |
| MAP-UT-006 | Update work schedule entity | Existing entity + valid request | Schedule fields overwritten | Mapping logic |
| MAP-UT-007 | Map doctor-work-schedule request to entity | Valid link request + linked doctor/schedule | Entity status and refs populated | Association mapping |
| MAP-UT-008 | Update doctor-work-schedule entity | Existing link + replacement doctor/schedule | Status and refs overwritten | Association mapping |
| MAP-UT-009 | Handle null mapper inputs safely | Null input(s) | Returns null or leaves target unchanged | Defensive behavior |

### EntityLifecycleTest
| Test Case ID | Test Objective | Input | Expected Output | Notes |
|---|---|---|---|---|
| LIFE-UT-001 | Auto-generate degree ID | `DoctorDegree` with null ID | `id` generated in `onCreate()` | Lifecycle callback |
| LIFE-UT-002 | Auto-generate schedule ID | `WorkSchedule` with null ID | `id` generated in `onCreate()` | Lifecycle callback |
| LIFE-UT-003 | Initialize doctor-work-schedule fields | `DoctorWorkSchedule` with null ID/timestamps | `id`, `createdAt`, `updatedAt` set | UTC timestamp rule |
| LIFE-UT-004 | Refresh update timestamp | Existing `DoctorWorkSchedule` + old `updatedAt` | `updatedAt` becomes newer | Lifecycle callback |

## 1.4. Project Link
- Repository URL: `https://github.com/son023/ThietKeVaTrienKhaiHeThongNhaKhoaThongMinhTichHopChatBotAI`
- Unit test script location in repo:
  - `service/doctor-service/src/test/java/com/main_project/doctor_service/service/DoctorServiceTest.java`
  - `service/doctor-service/src/test/java/com/main_project/doctor_service/service/DoctorDegreeServiceTest.java`
  - `service/doctor-service/src/test/java/com/main_project/doctor_service/service/DoctorWorkScheduleServiceTest.java`
  - `service/doctor-service/src/test/java/com/main_project/doctor_service/service/WorkScheduleServiceTest.java`
  - `service/doctor-service/src/test/java/com/main_project/doctor_service/entity/DoctorEntityTest.java`
  - `service/doctor-service/src/test/java/com/main_project/doctor_service/entity/EntityLifecycleTest.java`
  - `service/doctor-service/src/test/java/com/main_project/doctor_service/util/EntityMapperTest.java`

## 1.5. Execution Report
- Run command:
  - `./gradlew test`
- Execution date:
  - `2026-05-13`
- Result summary:
  - Total tests: `57`
  - Passed: `57`
  - Failed: `0`
  - Ignored: `0`
  - Duration: `4.903s`
- Evidence files:
  - Console summary showing passed/failed tests
  - HTML report at `build/reports/tests/test/index.html`
- Screenshot recommendation:
  - Open `build/reports/tests/test/index.html` and capture the summary card showing `57 tests`, `0 failures`, `0 ignored`.
  - Console output now also prints each individual test case with `PASSED`/`FAILED`, for example: `DoctorServiceTest > DOC-SRV-UT-001 ... PASSED`.

## 1.6. Code Coverage Report
- Run command:
  - `./gradlew test jacocoTestReport`
- Coverage report output:
  - HTML: `build/reports/jacoco/test/html/index.html`
  - XML: `build/reports/jacoco/test/jacocoTestReport.xml`
- Coverage summary:
  - Overall module line coverage: `35%` (`282 covered / 501 total`)
  - Overall module branch coverage: `8%` (`44 covered / 504 total`)
  - Targeted `service` package line coverage: `97%` (`92 covered / 94 total`)
  - Targeted `service` package branch coverage: `100%` (`10 covered / 10 total`)
  - `entity` package line coverage: `76%`
  - `util` package line coverage: `68%`
- Interpretation:
  - Overall coverage increased because business logic in aggregate/entity and mapper was added to the test scope.
  - Service-layer business logic remains strongly covered.
- Screenshot recommendation:
  - Open `build/reports/jacoco/test/html/com.main_project.doctor_service.service/index.html` and capture the `Total` row for the service package.

## 1.7. References and prompts used
### References
- Course requirement text provided in the request
- Repository source code under `service/doctor-service`
- Mockito + JUnit 5 via Spring Boot Starter Test

### Prompts used
1. `e:\code\ThietKeVaTrienKhaiHeThongNhaKhoaThongMinhTichHopChatBotAI\service\doctor-service viết test cho tôi bằng junit và mockito theo yêu cầu`
2. `Provide detailed comments to ensure the source code is easy to understand. The test script must include a comment identifying the corresponding Test Case ID.`
