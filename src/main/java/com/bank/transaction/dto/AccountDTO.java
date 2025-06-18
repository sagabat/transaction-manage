package com.bank.transaction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AccountDTO {
    private Long accountId;
    
    @NotNull(message = "客户ID不能为空")
    private Long customerId;
    
    @NotNull(message = "账户类型不能为空")
    private String accountType;
    
    @NotNull(message = "币种不能为空")
    private String currency = "CNY";
    
    @PositiveOrZero(message = "余额不能为负数")
    private BigDecimal balance;
    
    private Boolean delFlag;
} 