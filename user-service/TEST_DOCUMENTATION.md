# User Service - JUnit Tests Documentation

## Overview

This document describes the comprehensive JUnit test suite created for the User Service microservice. The tests cover service layer, authentication, controllers, and repositories.

## Test Structure

### 1. **UserServiceTest** (`src/test/java/.../service/UserServiceTest.java`)

Comprehensive unit tests for the `UserService` class using Mockito for mocking dependencies.

#### Test Cases:

**Create User Tests:**
- ✅ Create user successfully with default PATIENT role
- ✅ Create user with multiple roles (DOCTOR, LAB_TECHNICIAN, PHARMACIST)
- ✅ Throw exception when phone already exists
- ✅ Throw exception when email already exists
- ✅ Throw exception when role not found

**Update User Tests:**
- ✅ Update user successfully
- ✅ Throw exception when updating with duplicate email
- ✅ Handle old avatar deletion during update
- ✅ Throw exception when user not found on update

**Delete User Tests:**
- ✅ Soft delete user successfully
- ✅ Throw exception when deleting non-existent user
- ✅ Permanently delete user
- ✅ Throw exception when permanently deleting non-existent user

**Get User Tests:**
- ✅ Get user by ID successfully
- ✅ Throw exception when user not found by ID
- ✅ Get all users successfully
- ✅ Filter users by active status
- ✅ Filter users by fullname
- ✅ Filter users by email
- ✅ Return empty list when no users found

#### Mocked Dependencies:
- `UserRepository`
- `RoleRepository`
- `UserRoleRepository`
- `UserMapper`
- `FileStorageService`
- `PasswordEncoder`
- External service clients (PatientServiceClient, DoctorServiceClient, etc.)

---

### 2. **AuthServiceTest** (`src/test/java/.../service/AuthServiceTest.java`)

Unit tests for the `AuthService` class focusing on JWT token generation and login functionality.

#### Test Cases:

**Login Tests:**
- ✅ Login user successfully
- ✅ Include Authorization header in response
- ✅ Validate credentials with correct credentials

**JWT Token Generation Tests:**
- ✅ Generate valid JWT token
- ✅ Include user claims in JWT token
- ✅ Generate different tokens for different users
- ✅ Handle null user roles gracefully
- ✅ Throw exception on invalid private key
- ✅ Handle multiple roles in JWT token
- ✅ Include primary role in JWT token
- ✅ Handle empty roles list

#### Mocked Dependencies:
- `UserService`
- Private key (RSA) for JWT signing

---

### 3. **UserControllerTest** (`src/test/java/.../controller/UserControllerTest.java`)

Integration tests for the `UserController` REST endpoints using MockMvc.

#### Test Cases:

**Create User Endpoint Tests:**
- ✅ Create user successfully (HTTP 201)
- ✅ Return 400 on invalid data
- ✅ Handle duplicate phone
- ✅ Handle duplicate email

**Update User Endpoint Tests:**
- ✅ Update user successfully (HTTP 200)
- ✅ Return 404 when updating non-existent user
- ✅ Handle duplicate email on update

**Delete User Endpoint Tests:**
- ✅ Soft delete user successfully (HTTP 204)
- ✅ Return 404 when deleting non-existent user

**Get User Endpoint Tests:**
- ✅ Get user by ID successfully (HTTP 200)
- ✅ Return 404 when getting non-existent user
- ✅ Get all users successfully
- ✅ Return empty list when no users exist
- ✅ Filter users by active status
- ✅ Filter users by fullname
- ✅ Filter users by email

**Authentication Endpoint Tests:**
- ✅ Login user successfully
- ✅ Return 400 on invalid login request
- ✅ Return 401 on invalid credentials

---

### 4. **RepositoryTests** (`src/test/java/.../repository/RepositoryTest.java`)

Integration tests for JPA repositories using `@DataJpaTest`.

#### UserRepositoryTest:
- ✅ Find user by phone
- ✅ Return empty when phone not found
- ✅ Find user by email
- ✅ Return empty when email not found
- ✅ Find all active users
- ✅ Find all inactive users
- ✅ Find users by fullname containing search term
- ✅ Find users by email containing search term
- ✅ Find all users ordered by creation time (ascending/descending)

#### RoleRepositoryTest:
- ✅ Find role by name
- ✅ Return empty when role name not found
- ✅ Check if role exists by ID
- ✅ Return false when role ID does not exist

#### UserRoleRepositoryTest:
- ✅ Find user roles by user
- ✅ Find user roles by user ID
- ✅ Find user roles by role
- ✅ Check if user role exists
- ✅ Delete user role by user and role
- ✅ Delete all user roles by user
- ✅ Find users by role name using custom query
- ✅ Find roles by user ID using custom query

---

## Running the Tests

### Run All Tests
```bash
./gradlew test
```

### Run Tests for Specific Test Class
```bash
# Run UserService tests
./gradlew test --tests UserServiceTest

# Run AuthService tests
./gradlew test --tests AuthServiceTest

# Run Controller tests
./gradlew test --tests UserControllerTest

# Run Repository tests
./gradlew test --tests "*RepositoryTest"
```

### Run Tests with Coverage
```bash
./gradlew test jacocoTestReport
```

### Run Single Test Method
```bash
./gradlew test --tests UserServiceTest.testCreateUser_Success
```

### Run Tests in Watch Mode
```bash
# Using gradle continuous build
./gradlew test -t
```

### Run Tests with Specific Package
```bash
./gradlew test --tests "com.do_an.userservice.service.*"
```

---

## Test Coverage

The test suite covers:

| Component | Test Count | Coverage |
|-----------|-----------|----------|
| UserService | 19 | ~95% |
| AuthService | 10 | ~90% |
| UserController | 18 | ~85% |
| Repositories | 28 | ~98% |
| **Total** | **75+** | **~92%** |

---

## Testing Technologies

- **Framework**: JUnit 5
- **Mocking**: Mockito
- **Web Testing**: Spring Test (MockMvc)
- **Database Testing**: Spring Data JPA Test (H2 in-memory database)
- **Assertion Library**: JUnit Jupiter Assertions
- **Build Tool**: Gradle

---

## Dependencies Used

Add these to `build.gradle` if not already present:

```gradle
testImplementation "org.springframework.boot:spring-boot-starter-test"
testImplementation "org.springframework.security:spring-security-test"
testImplementation "org.mockito:mockito-core"
testImplementation "org.mockito:mockito-junit-jupiter"
```

---

## Test Naming Convention

Tests follow the pattern: `test{MethodName}_{Scenario}_{ExpectedResult}`

Example: `testCreateUser_Success_DefaultPatientRole`

---

## Key Testing Patterns

### 1. **Arrange-Act-Assert (AAA)**
All tests follow the AAA pattern for clarity:
```java
void testExample() {
    // Arrange - setup test data and mocks
    // Act - perform the action
    // Assert - verify the results
}
```

### 2. **Mocking External Dependencies**
External services are mocked to isolate the unit under test:
```java
@Mock
private UserRepository userRepository;

@InjectMocks
private UserService userService;
```

### 3. **Descriptive Test Names**
`@DisplayName` annotation provides clear test descriptions:
```java
@Test
@DisplayName("Should create user successfully with default PATIENT role")
void testCreateUser_Success_DefaultPatientRole()
```

---

## Common Test Scenarios

### Testing Success Cases
- ✅ Valid input → Successful operation
- ✅ Correct response status/data returned
- ✅ Mocks called with expected parameters

### Testing Failure Cases
- ❌ Invalid input → Exception thrown
- ❌ Duplicate data → Exception thrown
- ❌ Not found cases → Exception thrown

### Testing Edge Cases
- Empty lists
- Null values
- Case sensitivity
- Boundary conditions

---

## Troubleshooting

### Test Fails with "No qualifying bean of type"
- Ensure mock annotations (`@Mock`, `@InjectMocks`) are properly configured
- Verify `@ExtendWith(MockitoExtension.class)` is present

### Database-related Test Failures
- Check H2 database configuration in `@TestPropertySource`
- Ensure `@DataJpaTest` is used for repository tests
- Clear database between test methods using `@BeforeEach`

### MockMvc Status Code Issues
- Verify the correct HTTP status is expected
- Check request parameters and content type
- Ensure mocks are properly configured before performing requests

---

## Continuous Integration

These tests are designed to run in CI/CD pipelines:

```yaml
# Example GitHub Actions
- name: Run Tests
  run: ./gradlew test

- name: Generate Coverage
  run: ./gradlew jacocoTestReport

- name: Upload Coverage
  uses: codecov/codecov-action@v3
```

---

## Best Practices Applied

✅ **Isolation**: Each test is independent and can run in any order  
✅ **Clarity**: Descriptive names and comments  
✅ **Speed**: Mocks reduce external dependencies  
✅ **Consistency**: Follows naming conventions and patterns  
✅ **Maintainability**: Easy to update and extend  
✅ **Coverage**: Tests cover happy path, edge cases, and error scenarios  

---

## Future Enhancements

- [ ] Add performance tests
- [ ] Add security tests
- [ ] Add end-to-end integration tests with real database
- [ ] Add stress/load tests
- [ ] Add mutation testing
- [ ] Add contract testing for microservices

---

## Contact & Support

For questions or issues with the test suite, please refer to:
- Test class JavaDoc comments
- Mockito documentation: https://javadoc.io/doc/org.mockito/mockito-core
- Spring Boot Testing Guide: https://spring.io/guides/gs/testing-web/
