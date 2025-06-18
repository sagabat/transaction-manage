package com.bank.transaction.repository;

import com.bank.transaction.entity.OutgoingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OutgoingTransactionRepository extends JpaRepository<OutgoingTransaction, Long> {
    List<OutgoingTransaction> findByAccountIdAndDelFlagFalse(Long accountId);
    Optional<OutgoingTransaction> findByTransactionIdAndDelFlagFalse(Long transactionId);
} 