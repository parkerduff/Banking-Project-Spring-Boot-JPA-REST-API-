package com.sr_banking.banking_project.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank(message = "accountHolderName is required")
        String accountHolderName,

        @NotBlank(message = "accountType is required")
        @Pattern(regexp = "SAVINGS|CURRENT", message = "accountType must be SAVINGS or CURRENT")
        String accountType,

        @NotNull(message = "initialDeposit is required")
        @DecimalMin(value = "0.0", message = "initialDeposit cannot be negative")
        BigDecimal initialDeposit) {
}
