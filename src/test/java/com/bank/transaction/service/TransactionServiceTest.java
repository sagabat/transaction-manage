package com.bank.transaction.service;

import com.bank.transaction.dto.TransactionRequest;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.enums.TransactionType;
import com.bank.transaction.repository.TransactionLogRepository;
import com.bank.transaction.repository.TransactionRepository;
import com.bank.transaction.service.impl.TransactionServiceImpl;
import com.bank.transaction.util.TokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TokenUtil tokenUtil;

    @Mock
    private TransactionLogRepository transactionLogRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private TransactionRequest request;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        request = new TransactionRequest();
        request.setInAccount(1L);
        request.setOutAccount(2L);
        request.setAmount(new BigDecimal("100.00"));
        request.setTransactionType(TransactionType.TRANSFER);
        request.setToken("token valid");
    }

    @Test
    void createTransaction_Success() {
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);
        assertDoesNotThrow(() -> transactionService.createTransaction(request));

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void deleteTransaction_NotFound() {
        when(transactionRepository.findByTransactionIdAndDelFlagFalse(1L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                transactionService.deleteTransaction(1L));
    }

    @Test
    void listTransactions_Success() {
        List<Transaction> transactions = Collections.singletonList(transaction);
        when(transactionRepository.findByInAccountOrOutAccountAndDelFlagFalse(1L, 1L))
                .thenReturn(transactions);

        List<Transaction> result = transactionService.listTransactions(1L, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository).findByTransactionIdAndDelFlagFalse(1L);
    }
} 