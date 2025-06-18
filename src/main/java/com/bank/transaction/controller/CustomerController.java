package com.bank.transaction.controller;

import com.bank.transaction.dto.CustomerDTO;
import com.bank.transaction.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "客户管理", description = "客户管理相关接口")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @Operation(summary = "创建新客户")
    public ResponseEntity<CustomerDTO> createCustomer(
            @Parameter(description = "客户信息") @Valid @RequestBody CustomerDTO customerDTO) {
        return new ResponseEntity<>(customerService.createCustomer(customerDTO), HttpStatus.CREATED);
    }

    @DeleteMapping("/{customerId}")
    @Operation(summary = "删除客户")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "客户ID") @PathVariable Long customerId) {
        customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{customerId}")
    @Operation(summary = "更新客户信息")
    public ResponseEntity<CustomerDTO> updateCustomer(
            @Parameter(description = "客户ID") @PathVariable Long customerId,
            @Parameter(description = "客户信息") @Valid @RequestBody CustomerDTO customerDTO) {
        return ResponseEntity.ok(customerService.updateCustomer(customerId, customerDTO));
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "获取指定客户信息")
    public ResponseEntity<CustomerDTO> getCustomer(
            @Parameter(description = "客户ID") @PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getCustomerById(customerId));
    }
} 