package com.tummyfull.booking_app.services;

import com.tummyfull.booking_app.models.Employee;
import com.tummyfull.booking_app.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    // Create a new employee
    public Employee createEmployee(Employee employee) {
        // Set creation and update timestamps
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());

        return employeeRepository.save(employee);
    }

    // Get all employees
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    // Get an employee by ID
    public Optional<Employee> getEmployeeById(String id) {
        return employeeRepository.findById(id);
    }

     // Get an employee by email
    public Optional<Employee> getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }


    // Update an existing employee
    public Optional<Employee> updateEmployee(String id, Employee employeeDetails) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(id);

        if (optionalEmployee.isPresent()) {
            Employee existingEmployee = optionalEmployee.get();
            existingEmployee.setEmail(employeeDetails.getEmail());
            existingEmployee.setVerified(employeeDetails.isVerified());
            existingEmployee.setUpdatedAt(LocalDateTime.now()); // Update timestamp on update

            Employee updatedEmployee = employeeRepository.save(existingEmployee);
            return Optional.of(updatedEmployee);
        } else {
            return Optional.empty(); // Employee not found
        }
    }

    // Delete an employee by ID
    public boolean deleteEmployee(String id) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(id);
         if (optionalEmployee.isPresent()) {
             employeeRepository.deleteById(id);
             return true; // Deletion successful
         } else {
             return false; // Employee not found
         }
    }
}