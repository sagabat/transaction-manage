package com.bank.transaction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Customers")
@Data
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @NotBlank(message = "姓名不能为空")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "电话号码不能为空")
    @Pattern(regexp = "^\\d{11}$", message = "请输入11位有效的电话号码")
    @Column(unique = true, nullable = false)
    private String phoneNumber;

    private String address;

    private Boolean delFlag = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 