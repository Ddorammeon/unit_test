package com.do_an.prescriptionbillingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PrescriptionBillingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrescriptionBillingServiceApplication.class, args);
    }

}
