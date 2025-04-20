package com.tummyfull.booking_app.repositories;

import com.tummyfull.booking_app.models.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EmployeeRepository extends MongoRepository<Employee, String> {
    // Spring Data MongoDB provides basic CRUD operations automatically
    // We might add custom methods later, e.g., find by email
    Optional<Employee> findByEmail(String email);
}