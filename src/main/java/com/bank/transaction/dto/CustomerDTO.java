package com.bank.transaction.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CustomerDTO {
    private Long customerId;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "电话号码不能为空")
    @Pattern(regexp = "^\\d{11}$", message = "请输入11位有效的电话号码")
    private String phoneNumber;

    private String address;
} 