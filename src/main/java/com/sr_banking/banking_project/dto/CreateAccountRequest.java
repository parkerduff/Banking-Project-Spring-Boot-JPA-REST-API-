package com.sr_banking.banking_project.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class CreateAccountRequest {

    @NotBlank(message = "accountHolderName is required")
    @Size(max = 100, message = "accountHolderName must be at most 100 characters")
    private String accountHolderName;

    @NotBlank(message = "accountType is required")
    @Pattern(regexp = "SAVINGS|CURRENT", message = "accountType must be SAVINGS or CURRENT")
    private String accountType;

    @NotNull(message = "initialDeposit is required")
    @DecimalMin(value = "0.0", message = "initialDeposit must not be negative")
    private BigDecimal initialDeposit;

    // ISO 4217 currency code; defaults to SGD when omitted
    @Pattern(regexp = "[A-Z]{3}", message = "currency must be a 3-letter ISO 4217 code")
    private String currency;

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getInitialDeposit() {
        return initialDeposit;
    }

    public void setInitialDeposit(BigDecimal initialDeposit) {
        this.initialDeposit = initialDeposit;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
