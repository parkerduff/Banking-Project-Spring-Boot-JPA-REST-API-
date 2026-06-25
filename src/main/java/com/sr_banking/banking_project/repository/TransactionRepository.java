package com.sr_banking.banking_project.repository;

import com.sr_banking.banking_project.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByBankAccountAccountNumberOrderByTransactionDateDesc(String accountNumber, Pageable pageable);
}
