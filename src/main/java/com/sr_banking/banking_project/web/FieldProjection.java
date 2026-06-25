package com.sr_banking.banking_project.web;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.util.StringUtils;

/**
 * Implements client-driven field projection (ABS-MAS Playbook: "Support field projections on
 * resources by allowing clients to reduce the number of fields that come back in the response").
 * Callers pass a comma-separated {@code fields} query parameter; only those properties are returned.
 */
public final class FieldProjection {

    public static final String FILTER_ID = "fieldFilter";

    private FieldProjection() {
    }

    /** Wraps a response body, restricting serialized properties to {@code fields} when provided. */
    public static MappingJacksonValue apply(Object body, String fields) {
        MappingJacksonValue wrapper = new MappingJacksonValue(body);
        SimpleBeanPropertyFilter filter;
        if (StringUtils.hasText(fields)) {
            Set<String> selected = Arrays.stream(fields.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toSet());
            filter = SimpleBeanPropertyFilter.filterOutAllExcept(selected);
        } else {
            filter = SimpleBeanPropertyFilter.serializeAll();
        }
        FilterProvider provider = new SimpleFilterProvider().addFilter(FILTER_ID, filter);
        wrapper.setFilters(provider);
        return wrapper;
    }
}
