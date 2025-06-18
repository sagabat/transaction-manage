package com.bank.transaction.service;

import com.bank.transaction.dto.TransactionRequest;
import com.bank.transaction.entity.Transaction;

import java.util.List;

public interface TransactionService {
    /**
     * 创建交易
     */
    void createTransaction(TransactionRequest request);

    /**
     * 删除交易
     */
    void deleteTransaction(Long transactionId);

    /**
     * 修改交易
     */
    void modifyTransaction(Long transactionId, TransactionRequest request);

    /**
     * 查询账户的所有交易记录
     */
    List<Transaction> listTransactions(Long accountId, Integer page, Integer size);

    /**
     * 模拟前端产生token
     */
    String generateTransactionToken();
} 