package com.main_project.appointment_service.configuration;

import com.main_project.appointment_service.configuration.filter.CustomRequestFilter;
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
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/appointment-service/medical-services/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/appointment-service/appointments/doctor/**").permitAll()
                                .requestMatchers("/appointment-service/appointments/patient/**").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/appointment-service/appointments/**").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/appointment-service/appointments/**").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(customRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
