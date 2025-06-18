package com.bank.transaction.service;

import com.bank.transaction.dto.TransactionRequest;
import com.bank.transaction.entity.IncomingTransaction;
import com.bank.transaction.entity.OutgoingTransaction;
import com.bank.transaction.entity.Transaction;

import java.util.List;

public interface TransactionService {
    /**
     * 创建交易
     * 如果是转账类型，会同时创建转出和转入记录
     */
    void createTransaction(TransactionRequest request);

    /**
     * 删除交易
     * 如果是转账类型，会同时删除转出和转入记录
     */
    void deleteTransaction(Long transactionId);

    /**
     * 修改交易
     * 如果是转账类型，会同时修改转出和转入记录
     */
    void modifyTransaction(Long transactionId, TransactionRequest request);

    /**
     * 查询账户的转出交易
     */
    List<OutgoingTransaction> listOutgoingTransactions(Long accountId);

    /**
     * 查询账户的转入交易
     */
    List<IncomingTransaction> listIncomingTransactions(Long accountId);

    /**
     * 查询账户的所有交易记录（包括转出和转入）
     */
    List<Transaction> listAllTransactions(Long accountId);

    /**
     * 模拟前端产生token
     */
    String generateToken();
} 