package com.sr_banking.banking_project.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record TransferRequest(
        @NotBlank(message = "fromAccount is required")
        String fromAccount,

        @NotBlank(message = "toAccount is required")
        String toAccount,

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than zero")
        BigDecimal amount,

        @Size(max = 255, message = "description must be at most 255 characters")
        String description) {
}
