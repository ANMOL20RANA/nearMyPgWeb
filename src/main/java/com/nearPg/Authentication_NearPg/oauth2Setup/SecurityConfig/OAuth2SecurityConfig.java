package com.nearPg.Authentication_NearPg.oauth2Setup.SecurityConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.nearPg.Authentication_NearPg.JwtSupport_TO_0TP.Config.JwtFilterComponent;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class OAuth2SecurityConfig {

        private final JwtFilterComponent jwtFilterComponent;
        private final CustomOAuth2UserService customOAuth2UserService;

        @Bean(name = "oauth2SecurityFilterChain")
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .securityMatcher("/oauth2/**", "/login/oauth2/**")
                                .csrf(csrf -> csrf.disable()) // Disable CSRF protection
                                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                                                .requestMatchers("/h2-console/**")
                                                .permitAll() // Allow H2 console
                                                // access for everyone
                                                .anyRequest().authenticated() // Require authentication for other
                                                                              // requests
                                )
                                .oauth2Login(oauth2Login -> oauth2Login
                                                .successHandler(new SimpleUrlAuthenticationSuccessHandler(
                                                                "/oauth2/v1/demo")) // Redirect on successful login
                                                .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                                                                .userService(customOAuth2UserService) // Use custom
                                                                                                      // OAuth2UserService
                                                ));
                http.addFilterBefore(jwtFilterComponent,
                                UsernamePasswordAuthenticationFilter.class); // Custom filter to handle
                                                                             // J
                http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin())); // Allow

                return http.build();
        }

}