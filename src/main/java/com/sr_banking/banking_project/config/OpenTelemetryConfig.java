package com.sr_banking.banking_project.config;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class OpenTelemetryConfig {

    private static final Logger logger = LoggerFactory.getLogger(OpenTelemetryConfig.class);

    @Value("${spring.application.name:banking-service}")
    private String serviceName;

    @Bean
    public Tracer tracer() {
        logger.info("Initializing OpenTelemetry Tracer for service: {}", serviceName);
        return GlobalOpenTelemetry.getTracer(serviceName);
    }
}
