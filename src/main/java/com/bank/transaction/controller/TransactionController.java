package com.bank.transaction.controller;

import com.bank.transaction.dto.TransactionRequest;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "API For Transaction Management")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "创建交易")
    public ResponseEntity<Void> createTransaction(@Validated @RequestBody TransactionRequest request) {
        transactionService.createTransaction(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{transactionId}")
    @Operation(summary = "删除交易")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long transactionId) {
        transactionService.deleteTransaction(transactionId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{transactionId}")
    @Operation(summary = "更新交易")
    public ResponseEntity<Void> modifyTransaction(
            @PathVariable Long transactionId,
            @Validated @RequestBody TransactionRequest request) {
        transactionService.modifyTransaction(transactionId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    @Operation(summary = "查询所有交易")
    public ResponseEntity<List<Transaction>> listTransactions(
            @RequestParam Long accountId,
            @RequestParam Integer page,
            @RequestParam Integer size) {
        return ResponseEntity.ok(transactionService.listTransactions(accountId, page, size));
    }

    @GetMapping("/token")
    @Operation(summary = "获取客户端交易token，防止重复提交")
    public ResponseEntity<String> getTransactionToken() {
        return ResponseEntity.ok(transactionService.generateTransactionToken());
    }
}
