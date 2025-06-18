package com.bank.transaction.service.impl;

import com.bank.transaction.dto.AccountDTO;
import com.bank.transaction.exception.ResourceNotFoundException;
import com.bank.transaction.repository.AccountRepository;
import com.bank.transaction.repository.CustomerRepository;
import com.bank.transaction.entity.Account;
import com.bank.transaction.service.AccountService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountServiceImpl(AccountRepository accountRepository, CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public AccountDTO createAccount(AccountDTO accountDTO) {
        // 验证客户是否存在
        customerRepository.findById(accountDTO.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + accountDTO.getCustomerId()));

        Account account = new Account();
        BeanUtils.copyProperties(accountDTO, account);
        account.setAccountType(Account.AccountType.valueOf(accountDTO.getAccountType()));
        account.setDelFlag(false);
        account = accountRepository.save(account);
        
        AccountDTO savedAccountDTO = new AccountDTO();
        BeanUtils.copyProperties(account, savedAccountDTO);
        savedAccountDTO.setAccountType(account.getAccountType().name());
        return savedAccountDTO;
    }

    @Override
    @Transactional
    public void deleteAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
        account.setDelFlag(true);
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public AccountDTO updateAccount(Long accountId, AccountDTO accountDTO) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));

        // 如果更新了客户ID，验证新客户是否存在
        if (!account.getCustomerId().equals(accountDTO.getCustomerId())) {
            customerRepository.findById(accountDTO.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + accountDTO.getCustomerId()));
        }

        BeanUtils.copyProperties(accountDTO, account, "accountId", "createdAt");
        account.setAccountType(Account.AccountType.valueOf(accountDTO.getAccountType()));
        account = accountRepository.save(account);

        AccountDTO updatedAccountDTO = new AccountDTO();
        BeanUtils.copyProperties(account, updatedAccountDTO);
        updatedAccountDTO.setAccountType(account.getAccountType().name());
        return updatedAccountDTO;
    }

    @Override
    public AccountDTO getAccountById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
        
        AccountDTO accountDTO = new AccountDTO();
        BeanUtils.copyProperties(account, accountDTO);
        accountDTO.setAccountType(account.getAccountType().name());
        return accountDTO;
    }

    @Override
    public List<AccountDTO> getAccountsByCustomerId(Long customerId) {
        // 验证客户是否存在
        customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        return accountRepository.findByCustomerIdAndDelFlagFalse(customerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private AccountDTO convertToDTO(Account account) {
        AccountDTO accountDTO = new AccountDTO();
        BeanUtils.copyProperties(account, accountDTO);
        accountDTO.setAccountType(account.getAccountType().name());
        return accountDTO;
    }
} 