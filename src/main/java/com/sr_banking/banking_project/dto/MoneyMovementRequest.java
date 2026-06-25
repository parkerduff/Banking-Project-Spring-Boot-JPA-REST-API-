package com.sr_banking.banking_project.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Request body for a deposit or withdrawal against a single account. */
public record MoneyMovementRequest(
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than zero")
        BigDecimal amount,

        @Size(max = 255, message = "description must be at most 255 characters")
        String description) {
}
