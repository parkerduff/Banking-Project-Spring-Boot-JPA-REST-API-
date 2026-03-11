package com.sr_banking.banking_project.repository;

import com.sr_banking.banking_project.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByBankAccountAccountNumber(String accountNumber);
}