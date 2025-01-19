package com.nearPg.Authentication_NearPg.JwtSupport_TO_0TP.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
        private final AuthenticationProvider authenticationProvider;
        private final JwtFilterComponent jwtFilterComponent;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(
                                                auth -> auth
                                                                .requestMatchers("/h2-console/**",
                                                                                "/api/v1/auth/resend-verification-otp/**",
                                                                                "/api/v1/auth/verify-Login-otp/**",
                                                                                "/api/v1/auth/login/**",
                                                                                "/api/v1/auth/verified/**",
                                                                                "/api/v1/auth/resend-login-otp/**",
                                                                                "/api/v1/auth/register/**")
                                                                .permitAll()
                                                                .anyRequest().authenticated());

                http
                                .headers(head -> head.frameOptions(frame -> frame.disable()))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider)
                                .addFilterBefore(jwtFilterComponent, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

}
