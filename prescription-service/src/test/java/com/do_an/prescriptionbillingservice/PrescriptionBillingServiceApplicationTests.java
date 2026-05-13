/*
 * =============================================================================
 * FILE: PrescriptionBillingServiceApplicationTests.java
 * MODULE UNDER TEST: PrescriptionBillingServiceApplication.java
 * DESCRIPTION: Unit-level smoke test for the Spring Boot application class
 *              without loading database-backed Spring context.
 * FRAMEWORK: JUnit 5
 * LIBRARIES: Spring Boot Test
 * AUTHOR: Student Team
 * DATE: 2026-05-12
 * =============================================================================
 */

package com.do_an.prescriptionbillingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrescriptionBillingServiceApplicationTests {

    // TC_PrescriptionBillingServiceApplication_ClassMetadata_001
    @Test
    void test_applicationClass_hasRequiredSpringAnnotations() {
        /*
         * Test Case ID : TC_PrescriptionBillingServiceApplication_ClassMetadata_001
         * Objective    : Verify the application class keeps required Spring Boot and Feign annotations.
         * Input        : PrescriptionBillingServiceApplication.class metadata.
         * Expected     : @SpringBootApplication and @EnableFeignClients annotations are present.
         */
        // --- Arrange: load class metadata without creating Spring ApplicationContext ---
        Class<PrescriptionBillingServiceApplication> class_application =
                PrescriptionBillingServiceApplication.class;

        // --- Act: read the annotations under test ---
        SpringBootApplication actual_springBootApplication =
                class_application.getAnnotation(SpringBootApplication.class);
        EnableFeignClients actual_enableFeignClients =
                class_application.getAnnotation(EnableFeignClients.class);

        // --- Assert: verify required annotations exist ---
        assertNotNull(actual_springBootApplication);
        assertNotNull(actual_enableFeignClients);
        assertTrue(class_application.getDeclaredMethods().length > 0);

        // --- CheckDB: not applicable because this smoke test does not access DB ---
        // --- Rollback: not applicable because no DB state is modified ---
    }
}
