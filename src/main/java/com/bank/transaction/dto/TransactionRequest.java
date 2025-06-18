package com.bank.transaction.dto;

import com.bank.transaction.enums.TransactionType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TransactionRequest {
    @NotNull(message = "转入账户ID不能为空，交易类型为取款时，传入-1")
    private Long inAccount;

    @NotNull(message = "转出账号ID不能为空，交易类型为存款时，传入-1")
    private Long outAccount;

    @NotNull(message = "交易类型不能为空")
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @NotNull(message = "交易金额不能为空")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "一次性token，防止重复提交或重放攻击")
    private String token;

    @NotNull(message = "分页数")
    private Integer page;

    @NotNull(message = "分页大小")
    private Integer Size;
} 