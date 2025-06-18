package com.bank.transaction.service.impl;

import com.bank.transaction.dto.TransactionRequest;
import com.bank.transaction.enums.TransactionStatus;
import com.bank.transaction.enums.TransactionType;
import com.bank.transaction.exception.InsufficientBalanceException;
import com.bank.transaction.exception.InvalidTransactionException;
import com.bank.transaction.repository.AccountRepository;
import com.bank.transaction.repository.IncomingTransactionRepository;
import com.bank.transaction.repository.OutgoingTransactionRepository;
import com.bank.transaction.repository.TransactionLogRepository;
import com.bank.transaction.entity.Account;
import com.bank.transaction.entity.IncomingTransaction;
import com.bank.transaction.entity.OutgoingTransaction;
import com.bank.transaction.entity.TransactionLog;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.service.TransactionService;
import com.bank.transaction.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);
    
    private final OutgoingTransactionRepository outgoingTransactionRepository;
    private final IncomingTransactionRepository incomingTransactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final TokenUtil tokenUtil;

    @Override
    @Transactional
    @CacheEvict(value = {"outgoingTransactions", "incomingTransactions", "allTransactions"}, allEntries = true)
    public void createTransaction(TransactionRequest request) {
        // 校验token信息，防止重复提交或重放攻击
        validateToken(request);

        TransactionType type = request.getTransactionType();
        
        // 记录交易创建开始
        logger.info("开始创建交易，类型：{}", type);

        // 创建转出交易记录
        OutgoingTransaction outgoing = OutgoingTransaction.builder()
                .accountId(request.getAccountId())
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .transactionType(type)
                .toAccountId(request.getTargetAccountId())
                .delFlag(false)
                .createdAt(LocalDateTime.now())
                .build();

        OutgoingTransaction savedOutgoing = processTransaction(outgoing);

        // 如果是转账类型，还需要创建转入交易记录
        if (TransactionType.TRANSFER == type) {
            logger.info("开始创建转入交易记录，交易ID：{}", savedOutgoing.getTransactionId());

            IncomingTransaction incoming = IncomingTransaction.builder()
                    .transactionId(savedOutgoing.getTransactionId())
                    .accountId(request.getTargetAccountId())
                    .customerId(request.getCustomerId())
                    .amount(request.getAmount())
                    .transactionType(TransactionType.TRANSFER)
                    .fromAccountId(request.getAccountId())
                    .delFlag(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            incomingTransactionRepository.save(incoming);
            logger.info("转入交易记录创建成功，交易ID：{}", savedOutgoing.getTransactionId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"outgoingTransactions", "incomingTransactions", "allTransactions"}, allEntries = true)
    public void deleteTransaction(Long transactionId) {
        OutgoingTransaction outgoing = null;
        try {
            // 记录删除开始
            logger.info("开始删除交易，ID：{}", transactionId);
            
            // 查找并软删除转出交易
            outgoing = outgoingTransactionRepository.findByTransactionIdAndDelFlagFalse(transactionId)
                    .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
            
            // 处理交易撤销逻辑
            processTransactionReversal(outgoing);
            
            outgoing.setDelFlag(true);
            outgoingTransactionRepository.save(outgoing);
            logger.info("转出交易已标记为删除，交易ID：{}", transactionId);

            // 如果是转账类型，还需要软删除对应的转入交易
            if (TransactionType.TRANSFER == outgoing.getTransactionType()) {
                logger.info("开始处理转入交易的删除，交易ID：{}", transactionId);
                
                // 根据transactionId查找对应的转入交易
                IncomingTransaction incoming = incomingTransactionRepository
                        .findByTransactionIdAndDelFlagFalse(outgoing.getTransactionId())
                        .orElse(null);
                
                if (incoming != null) {
                    incoming.setDelFlag(true);
                    incomingTransactionRepository.save(incoming);
                    logger.info("转入交易已标记为删除，交易ID：{}", transactionId);
                }
            }
        } catch (Exception e) {
            // 记录失败日志
            if (outgoing != null) {
                logTransaction(outgoing, TransactionStatus.FAILED, "交易删除失败：" + e.getMessage());
            }
            throw e; // 抛出异常触发事务回滚
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"outgoingTransactions", "incomingTransactions", "allTransactions"}, allEntries = true)
    public void modifyTransaction(Long transactionId, TransactionRequest request) {
        // 校验token信息，防止重复提交或重放攻击
        validateToken(request);

        OutgoingTransaction outgoing = null;
        try {
            // 记录修改开始
            logger.info("开始修改交易，ID：{}", transactionId);
            
            // 修改转出交易
            outgoing = outgoingTransactionRepository.findByTransactionIdAndDelFlagFalse(transactionId)
                    .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
            
            // 先处理原交易的撤销
            processTransactionReversal(outgoing);
            logger.info("原交易已撤销，交易ID：{}", transactionId);
            
            // 更新交易信息
            updateOutgoingTransaction(outgoing, request);
            
            // 处理新交易
            processTransaction(outgoing);
            
            outgoingTransactionRepository.save(outgoing);
            logger.info("转出交易修改完成，交易ID：{}", transactionId);

            // 如果是转账类型，还需要修改对应的转入交易
            if (TransactionType.TRANSFER == outgoing.getTransactionType()) {
                logger.info("开始修改转入交易，交易ID：{}", transactionId);
                
                // 根据transactionId查找对应的转入交易
                IncomingTransaction incoming = incomingTransactionRepository
                        .findByTransactionIdAndDelFlagFalse(outgoing.getTransactionId())
                        .orElse(null);
                
                if (incoming != null) {
                    updateIncomingTransaction(incoming, request);
                    incomingTransactionRepository.save(incoming);
                    logger.info("转入交易修改完成，交易ID：{}", transactionId);
                }
            }
        } catch (Exception e) {
            // 记录失败日志
            if (outgoing != null) {
                logTransaction(outgoing, TransactionStatus.FAILED, "交易修改失败：" + e.getMessage());
            }
            throw e; // 抛出异常触发事务回滚
        }
    }

    @Override
    @Cacheable(value = "outgoingTransactions", key = "#accountId")
    public List<OutgoingTransaction> listOutgoingTransactions(Long accountId) {
        return outgoingTransactionRepository.findByAccountIdAndDelFlagFalse(accountId);
    }

    @Override
    @Cacheable(value = "incomingTransactions", key = "#accountId")
    public List<IncomingTransaction> listIncomingTransactions(Long accountId) {
        return incomingTransactionRepository.findByAccountIdAndDelFlagFalse(accountId);
    }

    @Override
    @Cacheable(value = "allTransactions", key = "#accountId")
    public List<Transaction> listAllTransactions(Long accountId) {
        List<Transaction> allTransactions = new ArrayList<>();
        allTransactions.addAll(outgoingTransactionRepository.findByAccountIdAndDelFlagFalse(accountId));
        allTransactions.addAll(incomingTransactionRepository.findByAccountIdAndDelFlagFalse(accountId));
        return allTransactions;
    }

    @Override
    public String generateToken() {
        return tokenUtil.generateToken();
    }

    private void validateToken(TransactionRequest request) {
        String token = request.getToken();
        synchronized (token) {
            tokenUtil.validateToken(token);
        }
    }
    private void updateOutgoingTransaction(OutgoingTransaction transaction, TransactionRequest request) {
        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getTransactionType() != null) {
            transaction.setTransactionType(request.getTransactionType());
        }
        if (request.getTargetAccountId() != null) {
            transaction.setToAccountId(request.getTargetAccountId());
        }
    }

    private void updateIncomingTransaction(IncomingTransaction transaction, TransactionRequest request) {
        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getTargetAccountId() != null) {
            transaction.setFromAccountId(request.getAccountId());
            transaction.setAccountId(request.getTargetAccountId());
        }
    }

    @Transactional
    public OutgoingTransaction processTransaction(OutgoingTransaction transaction) {
        // 验证交易基本信息
        validateTransaction(transaction);
        
        // 获取源账户
        Account sourceAccount = accountRepository.findByAccountIdAndDelFlagFalse(transaction.getAccountId())
            .orElseThrow(() -> new InvalidTransactionException("源账户不存在"));

        try {
            switch (transaction.getTransactionType()) {
                case DEPOSIT:
                    handleDeposit(sourceAccount, transaction);
                    break;
                case WITHDRAWAL:
                    handleWithdrawal(sourceAccount, transaction);
                    break;
                case TRANSFER:
                    handleTransfer(sourceAccount, transaction);
                    break;
            }

            // 保存交易记录
            OutgoingTransaction savedTransaction = outgoingTransactionRepository.save(transaction);
            
            // 记录交易日志
            logTransaction(savedTransaction, TransactionStatus.COMPLETED, "交易成功");
            
            return savedTransaction;

        } catch (Exception e) {
            // 记录失败日志
            logTransaction(transaction, TransactionStatus.FAILED, e.getMessage());
            throw e;
        }
    }

    private void validateTransaction(OutgoingTransaction transaction) {
        if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("交易金额必须大于0");
        }

        // 验证to_account_id的合法性
        if (transaction.getTransactionType() == TransactionType.TRANSFER) {
            if (transaction.getToAccountId() == null) {
                throw new InvalidTransactionException("转账交易必须指定目标账户");
            }
            
            // 验证币种是否相同
            Account sourceAccount = accountRepository.findByAccountIdAndDelFlagFalse(transaction.getAccountId())
                    .orElseThrow(() -> new EntityNotFoundException("源账户不存在"));
            Account targetAccount = accountRepository.findByAccountIdAndDelFlagFalse(transaction.getToAccountId())
                    .orElseThrow(() -> new EntityNotFoundException("目标账户不存在"));
            
            if (!sourceAccount.getCurrency().equals(targetAccount.getCurrency())) {
                throw new InvalidTransactionException("不同币种账户之间不能直接转账");
            }
        } else {
            if (transaction.getToAccountId() != null) {
                throw new InvalidTransactionException("存款和取款交易不能指定目标账户");
            }
        }
    }

    private void handleDeposit(Account account, OutgoingTransaction transaction) {
        // 更新账户余额（增加）
        account.setBalance(account.getBalance().add(transaction.getAmount()));
        accountRepository.save(account);
    }

    private void handleWithdrawal(Account account, OutgoingTransaction transaction) {
        // 检查余额是否充足
        if (account.getBalance().compareTo(transaction.getAmount()) < 0) {
            throw new InsufficientBalanceException("账户余额不足");
        }
        
        // 更新账户余额（减少）
        account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        accountRepository.save(account);
    }

    private void handleTransfer(Account sourceAccount, OutgoingTransaction transaction) {
        // 检查余额是否充足
        if (sourceAccount.getBalance().compareTo(transaction.getAmount()) < 0) {
            throw new InsufficientBalanceException("账户余额不足");
        }

        // 获取目标账户
        Account targetAccount = accountRepository.findByAccountIdAndDelFlagFalse(transaction.getToAccountId())
            .orElseThrow(() -> new InvalidTransactionException("目标账户不存在"));

        // 更新源账户余额（减少）
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(transaction.getAmount()));
        accountRepository.save(sourceAccount);

        // 更新目标账户余额（增加）
        targetAccount.setBalance(targetAccount.getBalance().add(transaction.getAmount()));
        accountRepository.save(targetAccount);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void logTransaction(OutgoingTransaction transaction, TransactionStatus status, String message) {
        if (transaction == null) {
            logger.info("Transaction Log - Status: {}, Message: {}", status, message);
            return;
        }
        
        TransactionLog log = new TransactionLog();
        log.setTransactionId(transaction.getTransactionId());
        log.setStatus(status);
        log.setMessage(message);
        transactionLogRepository.save(log);
    }

    private void processTransactionReversal(OutgoingTransaction transaction) {
        // 获取源账户
        Account sourceAccount = accountRepository.findByAccountIdAndDelFlagFalse(transaction.getAccountId())
            .orElseThrow(() -> new InvalidTransactionException("源账户不存在"));

        try {
            switch (transaction.getTransactionType()) {
                case DEPOSIT:
                    // 撤销存款：减少余额
                    handleWithdrawal(sourceAccount, transaction);
                    break;
                case WITHDRAWAL:
                    // 撤销取款：增加余额
                    handleDeposit(sourceAccount, transaction);
                    break;
                case TRANSFER:
                    // 撤销转账：源账户增加余额，目标账户减少余额
                    handleTransferReversal(sourceAccount, transaction);
                    break;
            }
            
            // 记录交易日志
            logTransaction(transaction, TransactionStatus.REVERSED, "交易已撤销");

        } catch (Exception e) {
            // 记录失败日志
            logTransaction(transaction, TransactionStatus.FAILED, "交易撤销失败：" + e.getMessage());
            throw e;
        }
    }

    private void handleTransferReversal(Account sourceAccount, OutgoingTransaction transaction) {
        // 获取目标账户
        Account targetAccount = accountRepository.findByAccountIdAndDelFlagFalse(transaction.getToAccountId())
            .orElseThrow(() -> new InvalidTransactionException("目标账户不存在"));

        // 检查目标账户余额是否充足
        if (targetAccount.getBalance().compareTo(transaction.getAmount()) < 0) {
            throw new InsufficientBalanceException("目标账户余额不足，无法撤销转账");
        }

        // 更新源账户余额（增加）
        sourceAccount.setBalance(sourceAccount.getBalance().add(transaction.getAmount()));
        accountRepository.save(sourceAccount);

        // 更新目标账户余额（减少）
        targetAccount.setBalance(targetAccount.getBalance().subtract(transaction.getAmount()));
        accountRepository.save(targetAccount);
    }
}
