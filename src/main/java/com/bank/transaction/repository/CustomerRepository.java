package com.bank.transaction.repository;

import com.bank.transaction.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmailAndDelFlagFalse(String email);
    Optional<Customer> findByPhoneNumberAndDelFlagFalse(String phoneNumber);
    Optional<Customer> findByCustomerIdAndDelFlagFalse(Long customerId);
} 