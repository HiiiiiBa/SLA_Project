package com.sla.monitoring.security;

import com.sla.monitoring.security.filter.JwtAuthenticationFilter;
import com.sla.monitoring.security.handler.JwtAccessDeniedHandler;
import com.sla.monitoring.security.handler.JwtAuthenticationEntryPoint;
import com.sla.monitoring.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Central Spring Security 6 configuration for stateless JWT authentication.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] SWAGGER_ENDPOINTS = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register", "/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/auth/change-password").authenticated()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/prometheus", "/actuator/info").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers(SWAGGER_ENDPOINTS).permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/clients/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/slas/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/slas/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/slas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/slas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/reports/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/reports/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/reports/*/export/**").hasAnyRole("ADMIN", "MANAGER", "CLIENT", "EMPLOYEE")
                        .requestMatchers(HttpMethod.POST, "/api/incidents/*/comments").hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")
                        .requestMatchers(HttpMethod.POST, "/api/incidents/*/analyze").hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE", "CLIENT")
                        .requestMatchers(HttpMethod.POST, "/api/ai/chat").hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE", "CLIENT")
                        .requestMatchers(HttpMethod.GET, "/api/ai/executive-report", "/api/ai/executive-report/**")
                            .hasAnyRole("ADMIN", "MANAGER", "CLIENT", "EMPLOYEE")
                        .requestMatchers(HttpMethod.DELETE, "/api/ai/executive-report/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/ai/executive-report", "/api/ai/executive-report/**")
                            .hasAnyRole("ADMIN", "MANAGER", "CLIENT", "EMPLOYEE")
                        .requestMatchers(HttpMethod.POST, "/api/incidents").hasAnyRole("ADMIN", "MANAGER", "CLIENT")
                        .requestMatchers(HttpMethod.PUT, "/api/incidents/**").hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PATCH, "/api/incidents/*/assign").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/incidents/**").hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")
                        .requestMatchers(HttpMethod.DELETE, "/api/incidents/**").denyAll()
                        .requestMatchers(HttpMethod.POST, "/api/approval-requests").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/approval-requests", "/api/approval-requests/**")
                            .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/approval-requests/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/alerts/*/read", "/api/alerts/*/resolve")
                            .hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/alerts/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/projects/**", "/api/teams/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/slas/**", "/api/reports/**", "/api/projects/**").hasAnyRole("ADMIN", "CLIENT", "MANAGER", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/api/teams/**").hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/api/services/**").hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE", "CLIENT")
                        .requestMatchers(HttpMethod.POST, "/api/services/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/services/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/services/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/services/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/maintenance-windows/**").hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/api/maintenance-windows/**").hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")
                        .requestMatchers(HttpMethod.PATCH, "/api/maintenance-windows/**").hasAnyRole("ADMIN", "MANAGER", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/api/maintenance-windows/**")
                            .hasAnyRole("ADMIN", "CLIENT", "MANAGER", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "CLIENT", "MANAGER", "EMPLOYEE")
                        .requestMatchers("/api/org/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/projects/**", "/api/teams/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
