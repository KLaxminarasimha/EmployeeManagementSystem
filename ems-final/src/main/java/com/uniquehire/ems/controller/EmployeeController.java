package com.uniquehire.ems.controller;

import com.uniquehire.ems.dto.ApiResponse;
import com.uniquehire.ems.dto.EmployeeRequest;
import com.uniquehire.ems.service.interfaces.IEmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class EmployeeController {

    private final IEmployeeService employeeService;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String dept,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(
            ApiResponse.ok(employeeService.getAllEmployees(page, size, dept, status, search)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(employeeService.getEmployeeById(id)));
    }

    @GetMapping("/by-email")
    public ResponseEntity<?> getByEmail(@RequestParam String email) {
        return ResponseEntity.ok(ApiResponse.ok(employeeService.getEmployeeByEmail(email)));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(employeeService.createEmployee(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                     @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(
            ApiResponse.ok("Employee updated", employeeService.updateEmployee(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        employeeService.deactivateEmployee(id);
        return ResponseEntity.ok(ApiResponse.ok("Employee deactivated", null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Long id,
                                           @RequestParam String status) {
        return ResponseEntity.ok(
            ApiResponse.ok("Status updated", employeeService.changeStatus(id, status)));
    }

    @PostMapping("/{id}/photo")
    public ResponseEntity<?> uploadPhoto(@PathVariable Long id,
                                          @RequestParam("file") MultipartFile file) {
        String path = employeeService.uploadProfilePhoto(id, file);
        return ResponseEntity.ok(ApiResponse.ok("Photo uploaded", Map.of("path", path)));
    }

    @GetMapping("/{id}/reports")
    public ResponseEntity<?> getDirectReports(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(employeeService.getDirectReports(id)));
    }

    @GetMapping("/{id}/attendance-summary")
    public ResponseEntity<?> getAttendanceSummary(@PathVariable Long id,
                                                   @RequestParam String month) {
        return ResponseEntity.ok(ApiResponse.ok(
            employeeService.getAttendanceSummary(id, month)));
    }
}
