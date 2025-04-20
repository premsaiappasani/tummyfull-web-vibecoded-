package com.tummyfull.booking_app.controllers;

import com.tummyfull.booking_app.models.Employee;
import com.tummyfull.booking_app.services.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/employees") // Base path for employee related endpoints
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    // POST /api/employees
    // Create a new employee
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        Employee createdEmployee = employeeService.createEmployee(employee);
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    // GET /api/employees
    // Get all employees
    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    // GET /api/employees/{id}
    // Get employee by ID
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String id) {
        Optional<Employee> employee = employeeService.getEmployeeById(id);
        return employee.map(ResponseEntity::ok) // If employee is present, return 200 OK
                       .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build()); // Otherwise, return 404 Not Found
    }

     // GET /api/employees/search?email=...
     // Get employee by Email (Example of query parameter)
     @GetMapping("/search")
     public ResponseEntity<Employee> getEmployeeByEmail(@RequestParam String email) {
         Optional<Employee> employee = employeeService.getEmployeeByEmail(email);
         return employee.map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
     }


    // PUT /api/employees/{id}
    // Update employee by ID
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable String id, @RequestBody Employee employeeDetails) {
        Optional<Employee> updatedEmployee = employeeService.updateEmployee(id, employeeDetails);
        return updatedEmployee.map(ResponseEntity::ok) // If update successful, return 200 OK
                             .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build()); // Otherwise, return 404 Not Found
    }

    // DELETE /api/employees/{id}
    // Delete employee by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String id) {
        boolean deleted = employeeService.deleteEmployee(id);
        if (deleted) {
            return ResponseEntity.noContent().build(); // 204 No Content on successful deletion
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 Not Found if employee didn't exist
        }
    }
}