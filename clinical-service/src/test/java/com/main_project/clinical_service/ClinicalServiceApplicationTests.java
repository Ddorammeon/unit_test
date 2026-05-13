package com.main_project.clinical_service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class ClinicalServiceApplicationTests {

    @Test
    @DisplayName("CLN-SRV-UT-051 - main should start application bootstrap without throwing")
    void mainShouldStartApplicationBootstrapWithoutThrowing() {
        String[] args = {"clinical"};
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            ClinicalServiceApplication.main(args);
            springApplication.verify(() -> SpringApplication.run(ClinicalServiceApplication.class, args));
        }
    }
}
