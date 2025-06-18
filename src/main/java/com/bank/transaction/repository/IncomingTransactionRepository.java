package com.bank.transaction.repository;

import com.bank.transaction.entity.IncomingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncomingTransactionRepository extends JpaRepository<IncomingTransaction, Long> {
    List<IncomingTransaction> findByAccountIdAndDelFlagFalse(Long accountId);
    Optional<IncomingTransaction> findByTransactionIdAndDelFlagFalse(Long transactionId);
} 