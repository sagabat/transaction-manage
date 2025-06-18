package com.bank.transaction.entity;

import com.bank.transaction.enums.TransactionType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "IncomingTransactions")
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class IncomingTransaction extends Transaction {
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column
    private Long fromAccountId;
} 