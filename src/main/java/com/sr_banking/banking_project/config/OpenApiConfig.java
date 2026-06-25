package com.sr_banking.banking_project.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Publishes machine-readable OpenAPI documentation and a Swagger UI (ABS-MAS Playbook: detailed,
 * easily accessible documentation drives API adoption and governance).
 */
@Configuration
public class OpenApiConfig {

    private static final String BASIC_AUTH = "basicAuth";

    @Bean
    public OpenAPI bankingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banking API")
                        .version("v1")
                        .description("Retail banking REST API aligned to the ABS-MAS Finance-as-a-Service "
                                + "API Playbook (versioning, JSON contracts, validation, secure error "
                                + "handling and authentication).")
                        .license(new License().name("Apache 2.0")))
                .components(new Components().addSecuritySchemes(BASIC_AUTH, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("basic")))
                .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH));
    }
}
