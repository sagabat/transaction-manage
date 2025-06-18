package com.bank.transaction.service;

import com.bank.transaction.dto.CustomerDTO;

public interface CustomerService {
    CustomerDTO createCustomer(CustomerDTO customerDTO);
    CustomerDTO updateCustomer(Long customerId, CustomerDTO customerDTO);
    CustomerDTO getCustomerById(Long customerId);
    void deleteCustomer(Long customerId);
} 