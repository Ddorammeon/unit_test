package com.do_an.invoiceservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI invoiceServiceOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8086");
        devServer.setDescription("Development Server");

        Server prodServer = new Server();
        prodServer.setUrl("https://api.example.com");
        prodServer.setDescription("Production Server");


        Info info = new Info()
                .title("Invoice Service API")
                .version("1.0.0")
                .description("API Documentation for Invoice Service - Quản lý hóa đơn và chi tiết hóa đơn")
              ;

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}

