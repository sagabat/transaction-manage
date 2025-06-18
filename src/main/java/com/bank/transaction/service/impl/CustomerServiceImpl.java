package com.bank.transaction.service.impl;

import com.bank.transaction.dto.CustomerDTO;
import com.bank.transaction.repository.CustomerRepository;
import com.bank.transaction.entity.Customer;
import com.bank.transaction.service.CustomerService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        // 检查邮箱和电话是否已存在
        customerRepository.findByEmailAndDelFlagFalse(customerDTO.getEmail())
                .ifPresent(c -> {
                    throw new IllegalArgumentException("邮箱已存在");
                });
        customerRepository.findByPhoneNumberAndDelFlagFalse(customerDTO.getPhoneNumber())
                .ifPresent(c -> {
                    throw new IllegalArgumentException("电话号码已存在");
                });

        Customer customer = new Customer();
        BeanUtils.copyProperties(customerDTO, customer);
        customer = customerRepository.save(customer);
        customer.setDelFlag(false);
        CustomerDTO result = new CustomerDTO();
        BeanUtils.copyProperties(customer, result);
        return result;
    }

    @Override
    @Transactional
    public void deleteCustomer(Long customerId) {
        Customer customer = customerRepository.findByCustomerIdAndDelFlagFalse(customerId)
                .orElseThrow(() -> new EntityNotFoundException("客户不存在"));
        customer.setDelFlag(true);
        customerRepository.save(customer);
    }

    @Override
    @Transactional
    public CustomerDTO updateCustomer(Long customerId, CustomerDTO customerDTO) {
        Customer customer = customerRepository.findByCustomerIdAndDelFlagFalse(customerId)
                .orElseThrow(() -> new EntityNotFoundException("客户不存在"));

        // 检查邮箱和电话是否与其他用户重复
        customerRepository.findByEmailAndDelFlagFalse(customerDTO.getEmail())
                .ifPresent(c -> {
                    if (!c.getCustomerId().equals(customerId)) {
                        throw new IllegalArgumentException("邮箱已存在");
                    }
                });
        customerRepository.findByPhoneNumberAndDelFlagFalse(customerDTO.getPhoneNumber())
                .ifPresent(c -> {
                    if (!c.getCustomerId().equals(customerId)) {
                        throw new IllegalArgumentException("电话号码已存在");
                    }
                });

        BeanUtils.copyProperties(customerDTO, customer, "customerId", "delFlag", "createdAt");
        customer = customerRepository.save(customer);
        CustomerDTO result = new CustomerDTO();
        BeanUtils.copyProperties(customer, result);
        return result;
    }

    @Override
    public CustomerDTO getCustomerById(Long customerId) {
        Customer customer = customerRepository.findByCustomerIdAndDelFlagFalse(customerId)
                .orElseThrow(() -> new EntityNotFoundException("客户不存在"));
        CustomerDTO customerDTO = new CustomerDTO();
        BeanUtils.copyProperties(customer, customerDTO);
        return customerDTO;
    }
} 