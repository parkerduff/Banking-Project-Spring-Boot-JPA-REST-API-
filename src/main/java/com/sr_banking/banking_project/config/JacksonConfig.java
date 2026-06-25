package com.sr_banking.banking_project.config;

import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.sr_banking.banking_project.web.FieldProjection;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /**
     * Registers a default "serialize all" filter so responses annotated with {@code @JsonFilter}
     * still serialize correctly when no field projection is requested.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer fieldFilterCustomizer() {
        return builder -> builder.filters(new SimpleFilterProvider()
                .addFilter(FieldProjection.FILTER_ID, SimpleBeanPropertyFilter.serializeAll())
                .setFailOnUnknownId(false));
    }
}
