package com.sla.monitoring.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI slaMonitoringOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SLA Monitoring System API")
                        .description("Système Intelligent de Gestion des SLA - API Documentation")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("SLA Monitoring Team")
                                .email("support@sla-monitoring.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://sla-monitoring.com")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token obtained from /api/auth/login")));
    }
}
