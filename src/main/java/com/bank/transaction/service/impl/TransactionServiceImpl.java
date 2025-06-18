package com.bank.transaction.service.impl;

import com.bank.transaction.dto.TransactionRequest;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.repository.TransactionRepository;
import com.bank.transaction.service.TransactionService;
import com.bank.transaction.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);
    private final TransactionRepository transactionRepository;
    private final TokenUtil tokenUtil;

    private void validateToken(TransactionRequest request) {
        String token = request.getToken();
        // 对token加锁，保证多线程并发情况下，只能有一个线程持有该token，确保接口幂等
        synchronized (token) {
            tokenUtil.validateToken(token);
        }
    }

    @Override
    @Transactional()
    @CacheEvict(value = {"Transactions"}, allEntries = true)
    public void createTransaction(TransactionRequest request) {
        // 校验token信息，防止重复提交或重放攻击
        validateToken(request);
        
        // 记录交易创建开始
        logger.info("开始创建交易，类型：{}", request.getTransactionType());

        // 一次调用只新增一条交易记录，按题目的意思不考虑用户信息；实际业务中存在用户账户信息的变更，可以在这个接口中使用同一个事务处理
        Transaction transaction = new Transaction();
        transaction.setInAccount(request.getInAccount());
        transaction.setOutAccount(request.getOutAccount());
        transaction.setAmount(request.getAmount());
        transaction.setDelFlag(false);
        transaction.setTransactionType(request.getTransactionType());

        transactionRepository.save(transaction);

        // 记录交易创建结束
        logger.info("创建交易结束，类型：{}", request.getTransactionType());
    }

    @Override
    @Transactional()
    @CacheEvict(value = {"Transactions"}, allEntries = true)
    public void deleteTransaction(Long transactionId) {
        logger.info("开始删除交易，ID：{}", transactionId);
        // 查找并软删除交易,该接口逻辑上幂等，不需要额外进行幂等处理
        Transaction transaction = transactionRepository.findByTransactionIdAndDelFlagFalse(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
        transaction.setDelFlag(true);
        transactionRepository.save(transaction);
        logger.info("交易已标记为删除，交易ID：{}", transactionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"outgoingTransactions", "incomingTransactions", "allTransactions"}, allEntries = true)
    public void modifyTransaction(Long transactionId, TransactionRequest request) {
        // 校验token信息，防止重复提交或重放攻击
        validateToken(request);

        logger.info("开始修改交易，ID：{}", transactionId);
        Transaction transaction = transactionRepository.findByTransactionIdAndDelFlagFalse(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
        transaction.setAmount(request.getAmount());
        transaction.setOutAccount(request.getOutAccount());
        transaction.setInAccount(request.getInAccount());
        transaction.setTransactionType(request.getTransactionType());
        transactionRepository.save(transaction);
        logger.info("修改交易结束，ID：{}", transactionId);
    }

    // 提供分页查询功能
    @Override
    @Cacheable(value = "Transactions", key = "#page + '-' + #size")
    public List<Transaction> listTransactions(Long accountId, Integer page, Integer size) {
        return new ArrayList<>(transactionRepository.findByInAccountOrOutAccountAndDelFlagFalse(accountId, accountId)).stream()
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    @Override
    public String generateTransactionToken() {
        return tokenUtil.generateToken();
    }
}
