package com.bank.transaction.controller;

import com.bank.transaction.dto.AccountDTO;
import com.bank.transaction.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "账户管理", description = "账户管理相关接口")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @Operation(summary = "创建新账户")
    public ResponseEntity<AccountDTO> createAccount(
            @Parameter(description = "账户信息") @Valid @RequestBody AccountDTO accountDTO) {
        return new ResponseEntity<>(accountService.createAccount(accountDTO), HttpStatus.CREATED);
    }

    @DeleteMapping("/{accountId}")
    @Operation(summary = "删除账户")
    public ResponseEntity<Void> deleteAccount(
            @Parameter(description = "账户ID") @PathVariable Long accountId) {
        accountService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{accountId}")
    @Operation(summary = "更新账户信息")
    public ResponseEntity<AccountDTO> updateAccount(
            @Parameter(description = "账户ID") @PathVariable Long accountId,
            @Parameter(description = "账户信息") @Valid @RequestBody AccountDTO accountDTO) {
        return ResponseEntity.ok(accountService.updateAccount(accountId, accountDTO));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "获取指定账户信息")
    public ResponseEntity<AccountDTO> getAccount(
            @Parameter(description = "账户ID") @PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "获取指定客户的所有账户")
    public ResponseEntity<List<AccountDTO>> getAccountsByCustomerId(
            @Parameter(description = "客户ID") @PathVariable Long customerId) {
        return ResponseEntity.ok(accountService.getAccountsByCustomerId(customerId));
    }
} 