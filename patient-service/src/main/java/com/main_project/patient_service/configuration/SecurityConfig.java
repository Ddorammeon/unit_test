package com.main_project.patient_service.configuration;

import com.main_project.patient_service.configuration.filter.CustomRequestFilter;
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
//                .requestMatchers(HttpMethod.GET, "/patient-service/patients/**").hasAnyRole("DOCTOR", "RECEPTIONIST", "ADMIN")
//                .requestMatchers(HttpMethod.POST, "/patient-service/patients/**").hasAnyRole("RECEPTIONIST", "ADMIN")
//                .requestMatchers(HttpMethod.PUT, "/patient-service/patients/**").hasAnyRole("RECEPTIONIST", "ADMIN", "PATIENT")
                .requestMatchers("/patient-service/medical-histories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/patient-service/patients/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/patient-service/patients/**").permitAll()
                .requestMatchers(HttpMethod.PUT, "/patient-service/patients/**").permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(customRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}