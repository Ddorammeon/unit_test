package com.do_an.paymentservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI paymentServiceOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8087");
        devServer.setDescription("Development Server");

        Server prodServer = new Server();
        prodServer.setUrl("https://api.example.com");
        prodServer.setDescription("Production Server");


        Info info = new Info()
                .title("Payment Service API")
                .version("1.0.0")
                .description("API Documentation for Payment Service - Quản lý thanh toán hóa đơn. Hỗ trợ thanh toán tiền mặt và chuyển khoản qua PayOS.")
              ;

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}

