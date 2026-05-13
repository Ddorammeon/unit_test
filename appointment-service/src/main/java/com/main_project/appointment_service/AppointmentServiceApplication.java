package com.main_project.appointment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableCaching
@EnableFeignClients
public class AppointmentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppointmentServiceApplication.class, args);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> onApplicationReadyEvent() {
        return event -> {
            System.out.println("\n------------------------------------------");
            System.out.println("Swagger UI is available at: http://localhost:8082/api-docs");
            System.out.println("API Docs JSON is available at: http://localhost:8082/v3/api-docs");
            System.out.println("------------------------------------------\n");
        };
    }
}


