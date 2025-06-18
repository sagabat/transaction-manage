package com.bank.transaction.entity;

import com.bank.transaction.enums.TransactionType;
import lombok.Data;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(insertable = false, updatable = false)
    private Long transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private Long inAccount;

    @Column(nullable = false)
    private Long outAccount;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private Boolean delFlag = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
} 