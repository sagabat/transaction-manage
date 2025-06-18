package com.bank.transaction.repository;

import com.bank.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionIdAndDelFlagFalse(Long transactionId);
    List<Transaction> findByInAccountOrOutAccountAndDelFlagFalse(Long inAccount, Long outAccount);
}