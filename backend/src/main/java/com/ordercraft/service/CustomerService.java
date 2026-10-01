package com.ordercraft.service;

import com.ordercraft.dto.CustomerRequest;
import com.ordercraft.entity.Customer;
import com.ordercraft.exception.BadRequestException;
import com.ordercraft.exception.ResourceNotFoundException;
import com.ordercraft.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuditLogService auditLogService;

    public CustomerService(CustomerRepository customerRepository, AuditLogService auditLogService) {
        this.customerRepository = customerRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Page<Customer> getAllCustomers(String query, String status, Pageable pageable) {
        return customerRepository.searchCustomers(query, status, pageable);
    }

    @Transactional(readOnly = true)
    public List<Customer> getAllActiveCustomers() {
        return customerRepository.findAll().stream()
                .filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatus()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
    }

    @Transactional
    public Customer createCustomer(CustomerRequest req) {
        if (customerRepository.existsByCustomerCode(req.getCustomerCode())) {
            throw new BadRequestException("Customer code already exists: " + req.getCustomerCode());
        }

        Customer customer = new Customer();
        mapRequestToEntity(req, customer);
        Customer saved = customerRepository.save(customer);

        auditLogService.record("CUSTOMER_CREATED", "CUSTOMER", "Customer", saved.getId().toString(), null, saved.getName());
        return saved;
    }

    @Transactional
    public Customer updateCustomer(Long id, CustomerRequest req) {
        Customer customer = getCustomerById(id);

        if (customerRepository.existsByCustomerCodeAndIdNot(req.getCustomerCode(), id)) {
            throw new BadRequestException("Customer code is already used by another customer: " + req.getCustomerCode());
        }

        String oldVal = customer.getName() + " (" + customer.getCustomerCode() + ")";
        mapRequestToEntity(req, customer);
        Customer updated = customerRepository.save(customer);

        auditLogService.record("CUSTOMER_UPDATED", "CUSTOMER", "Customer", id.toString(), oldVal, updated.getName());
        return updated;
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = getCustomerById(id);
        if (!"INACTIVE".equalsIgnoreCase(customer.getStatus())) {
            customer.setStatus("INACTIVE");
            customerRepository.save(customer);
            auditLogService.record("CUSTOMER_DEACTIVATED", "CUSTOMER", "Customer", id.toString(), "ACTIVE", "INACTIVE");
        }
    }

    private void mapRequestToEntity(CustomerRequest req, Customer entity) {
        entity.setCustomerCode(req.getCustomerCode().trim());
        entity.setName(req.getName().trim());
        entity.setEmail(req.getEmail().trim());
        entity.setPhone(req.getPhone().trim());
        entity.setAddress(req.getAddress().trim());
        entity.setCity(req.getCity().trim());
        entity.setState(req.getState().trim());
        entity.setCountry(req.getCountry().trim());
        entity.setPostalCode(req.getPostalCode().trim());
        if (req.getStatus() != null && !req.getStatus().isBlank()) {
            entity.setStatus(req.getStatus().trim().toUpperCase());
        }
    }
}
