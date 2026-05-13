/*
 * =============================================================================
 * FILE: PatientServiceApplicationTests.java
 * MODULE UNDER TEST: PatientServiceApplication.java
 * DESCRIPTION: Unit-level smoke test for application metadata without loading
 *              database-backed Spring context.
 * FRAMEWORK: JUnit 5
 * LIBRARIES: Spring Boot Test
 * AUTHOR: Student Team
 * DATE: 2026-05-12
 * =============================================================================
 */

package com.main_project.patient_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PatientServiceApplicationTests {

    // TC_PatientServiceApplication_ClassMetadata_001
    @Test
    void test_applicationClass_hasSpringBootApplicationAnnotation() {
        /*
         * Test Case ID : TC_PatientServiceApplication_ClassMetadata_001
         * Objective    : Verify that the application class keeps the Spring Boot entry annotation.
         * Input        : PatientServiceApplication.class metadata.
         * Expected     : @SpringBootApplication annotation is present.
         */
        // --- Arrange: load class metadata without creating Spring ApplicationContext ---
        Class<PatientServiceApplication> class_application = PatientServiceApplication.class;

        // --- Act: read the annotation under test ---
        SpringBootApplication actual_springBootApplication =
                class_application.getAnnotation(SpringBootApplication.class);

        // --- Assert: verify required annotation exists ---
        assertNotNull(actual_springBootApplication);

        // --- CheckDB: not applicable because this smoke test does not access DB ---
        // --- Rollback: not applicable because no DB state is modified ---
    }
}
