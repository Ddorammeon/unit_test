package com.do_an.paymentservice.configuration;

import com.do_an.paymentservice.configuration.filter.CustomRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
              .requestMatchers("/payment-service/payments/**").permitAll()
                .requestMatchers("/api/payments/webhook/**").permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(customRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}