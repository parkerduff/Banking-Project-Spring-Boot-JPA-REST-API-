package com.sr_banking.banking_project.dto;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Standard error payload returned for every failed request. Provides a consistent,
 * machine-readable error contract (ABS-MAS Playbook: Error Handling / Data Standards)
 * without leaking internal exception details.
 */
public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorDetail> fieldErrors) {

    public record FieldErrorDetail(String field, String message) {
    }
}
