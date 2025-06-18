package com.bank.transaction.dto;

import com.bank.transaction.enums.TransactionType;
import lombok.Data;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TransactionRequest {
    @NotNull(message = "账户ID不能为空")
    private Long accountId;

    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    @NotNull(message = "交易类型不能为空")
    private TransactionType transactionType;

    @NotNull(message = "交易金额不能为空")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private Long targetAccountId; // for transfers (toAccountId or fromAccountId)

    @NotNull(message = "一次性token，防止重复提交或重放攻击")
    private String token;
} 