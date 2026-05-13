# Unit Testing Report - appointment-service

## Tools and Libraries
- JUnit 5
- Mockito
- AssertJ
- JaCoCo
- Gradle

## Scope of Testing
- Tested:
  - `AppointmentService`
  - `MedicalServiceService`
  - `SlotService`
  - `EntityDTOMapper`
- Not tested:
  - `controller/*`, `repository/*`, `config/*`, `feignclient/*`
  - bootstrap class and security/filter wiring
- DB note:
  - these are Mockito-based unit tests, so no real DB or Redis state is changed
  - repository and Redis interactions are verified by mock assertions

## Reports
- Test HTML: `build/reports/tests/test/index.html`
- Coverage HTML: `build/reports/jacoco/test/html/index.html`
- Custom TXT: `build/reports/custom-test-status/custom-test-status.txt`
- Custom HTML: `build/reports/custom-test-status/custom-test-status.html`
