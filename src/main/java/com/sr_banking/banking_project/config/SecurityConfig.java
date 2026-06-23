package com.sr_banking.banking_project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Baseline API security (ABS-MAS Playbook: Information Security — Authentication, Authorisation,
 * Encryption). This demo uses stateless HTTP Basic with BCrypt-hashed, role-based in-memory users
 * and enforces security response headers (HSTS, X-Content-Type-Options, frame options).
 *
 * <p>Production deployments should front this with TLS 1.2+ and replace HTTP Basic with the
 * playbook's recommended OAuth 2.0 / OpenID Connect flow validating signed JWT access tokens.
 * Credentials are sourced from configuration so they can be overridden via environment variables.
 */
@Configuration
public class SecurityConfig {

    private final String userName;
    private final String userPassword;
    private final String adminName;
    private final String adminPassword;

    public SecurityConfig(
            @Value("${app.security.user.name:user}") String userName,
            @Value("${app.security.user.password:changeit}") String userPassword,
            @Value("${app.security.admin.name:admin}") String adminName,
            @Value("${app.security.admin.password:changeit}") String adminPassword) {
        this.userName = userName;
        this.userPassword = userPassword;
        this.adminName = adminName;
        this.adminPassword = adminPassword;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsManager(PasswordEncoder passwordEncoder) {
        UserDetails reader = User.withUsername(userName)
                .password(passwordEncoder.encode(userPassword))
                .roles("USER")
                .build();
        UserDetails admin = User.withUsername(adminName)
                .password(passwordEncoder.encode(adminPassword))
                .roles("USER", "ADMIN")
                .build();
        return new InMemoryUserDetailsManager(reader, admin);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/health",
                                "/h2-console/**")
                        .permitAll()
                        // State-changing money operations require an elevated role.
                        .requestMatchers(HttpMethod.POST, "/api/v1/bank/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/bank/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/bank/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/bank/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)))
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
