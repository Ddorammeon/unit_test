package com.main_project.doctor_service.configuration;

import com.main_project.doctor_service.configuration.filter.CustomRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomRequestFilter customRequestFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf().disable()
                .authorizeHttpRequests(auth -> auth
                        // Allow unauthenticated access to Swagger/API docs
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        // Allow unauthenticated OPTIONS and GET requests to doctor endpoints (for public access)
                        .requestMatchers(HttpMethod.OPTIONS, "/doctor-service/doctors/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/doctor-service/doctors/**").permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(customRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}