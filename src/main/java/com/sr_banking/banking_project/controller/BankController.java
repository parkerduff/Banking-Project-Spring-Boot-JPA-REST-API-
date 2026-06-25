package com.sr_banking.banking_project.controller;

import com.sr_banking.banking_project.dto.AccountResponse;
import com.sr_banking.banking_project.dto.CreateAccountRequest;
import com.sr_banking.banking_project.dto.MoneyMovementRequest;
import com.sr_banking.banking_project.dto.PagedResponse;
import com.sr_banking.banking_project.dto.TransactionResponse;
import com.sr_banking.banking_project.dto.TransferRequest;
import com.sr_banking.banking_project.model.BankAccount;
import com.sr_banking.banking_project.model.Transaction;
import com.sr_banking.banking_project.service.BankService;
import com.sr_banking.banking_project.web.FieldProjection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Versioned, resource-oriented banking API. Endpoints use plural nouns (not verbs), accept and
 * return JSON, validate input, support pagination and field projection, and return typed response
 * DTOs — per the ABS-MAS Finance-as-a-Service API Playbook design guidelines.
 */
@RestController
@RequestMapping("/api/v1/bank")
@Validated
public class BankController {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String ACCOUNT_NUMBER_PATTERN = "^[A-Za-z0-9_-]{1,64}$";

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        BankAccount account = bankService.createAccount(
                request.accountHolderName(), request.accountType(), request.initialDeposit());
        AccountResponse body = AccountResponse.from(account);
        return ResponseEntity
                .created(URI.create("/api/v1/bank/accounts/" + account.getAccountNumber()))
                .body(body);
    }

    @GetMapping("/accounts")
    public ResponseEntity<?> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String fields) {
        Page<BankAccount> accounts = bankService.getAllAccounts(pageable(page, size));
        List<AccountResponse> content = accounts.map(AccountResponse::from).getContent();
        PagedResponse<AccountResponse> body = PagedResponse.from(accounts, content);
        return ResponseEntity.ok(FieldProjection.apply(body, fields));
    }

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<?> getAccount(
            @PathVariable @Pattern(regexp = ACCOUNT_NUMBER_PATTERN, message = "invalid account number")
                    String accountNumber,
            @RequestParam(required = false) String fields) {
        AccountResponse body = AccountResponse.from(bankService.getAccountByNumber(accountNumber));
        return ResponseEntity.ok(FieldProjection.apply(body, fields));
    }

    @PostMapping("/accounts/{accountNumber}/deposits")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable @Pattern(regexp = ACCOUNT_NUMBER_PATTERN, message = "invalid account number")
                    String accountNumber,
            @Valid @RequestBody MoneyMovementRequest request) {
        Transaction transaction = bankService.depositMoney(
                accountNumber, request.amount(), description(request.description(), "Cash deposit"));
        return ResponseEntity.ok(TransactionResponse.from(transaction));
    }

    @PostMapping("/accounts/{accountNumber}/withdrawals")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable @Pattern(regexp = ACCOUNT_NUMBER_PATTERN, message = "invalid account number")
                    String accountNumber,
            @Valid @RequestBody MoneyMovementRequest request) {
        Transaction transaction = bankService.withdrawMoney(
                accountNumber, request.amount(), description(request.description(), "Cash withdrawal"));
        return ResponseEntity.ok(TransactionResponse.from(transaction));
    }

    @GetMapping("/accounts/{accountNumber}/transactions")
    public ResponseEntity<PagedResponse<TransactionResponse>> getStatement(
            @PathVariable @Pattern(regexp = ACCOUNT_NUMBER_PATTERN, message = "invalid account number")
                    String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Transaction> statement = bankService.getAccountStatement(accountNumber, pageable(page, size));
        List<TransactionResponse> content = statement.map(TransactionResponse::from).getContent();
        return ResponseEntity.ok(PagedResponse.from(statement, content));
    }

    @PostMapping("/transfers")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        Transaction transaction = bankService.transferMoney(
                request.fromAccount(), request.toAccount(), request.amount(), request.description());
        return ResponseEntity.ok(TransactionResponse.from(transaction));
    }

    private static Pageable pageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize);
    }

    private static String description(String provided, String fallback) {
        return provided == null || provided.isBlank() ? fallback : provided;
    }
}
