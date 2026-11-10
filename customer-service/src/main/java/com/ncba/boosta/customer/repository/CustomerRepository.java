package com.ncba.boosta.customer.repository;

import com.ncba.boosta.customer.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, String> {
}
