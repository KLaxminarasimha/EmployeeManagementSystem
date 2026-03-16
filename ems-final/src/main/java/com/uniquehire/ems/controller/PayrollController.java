package com.uniquehire.ems.controller;

import com.uniquehire.ems.dto.*;
import com.uniquehire.ems.service.interfaces.IPayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class PayrollController {

    private final IPayrollService payrollService;

    @GetMapping
    public ResponseEntity<?> getByMonth(@RequestParam String month) {
        return ResponseEntity.ok(
            ApiResponse.ok(payrollService.getPayrollByMonth(month)));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(@RequestParam String month) {
        return ResponseEntity.ok(
            ApiResponse.ok(payrollService.getPayrollSummary(month)));
    }

    @GetMapping("/employee/{empId}")
    public ResponseEntity<?> getByEmployee(@PathVariable Long empId) {
        return ResponseEntity.ok(
            ApiResponse.ok(payrollService.getPayrollByEmployee(empId)));
    }

    @GetMapping("/employee/{empId}/month")
    public ResponseEntity<?> getByEmployeeAndMonth(@PathVariable Long empId,
                                                    @RequestParam String month) {
        return ResponseEntity.ok(
            ApiResponse.ok(payrollService.getPayrollByEmployeeAndMonth(empId, month)));
    }

    @PostMapping("/process")
    public ResponseEntity<?> bulkProcess(@Valid @RequestBody PayrollProcessRequest request) {
        return ResponseEntity.ok(
            ApiResponse.ok("Payroll processed successfully",
                payrollService.bulkProcessPayroll(request)));
    }

    @PostMapping("/process/{empId}")
    public ResponseEntity<?> processSingle(@PathVariable Long empId,
                                            @RequestParam String month) {
        return ResponseEntity.ok(
            ApiResponse.ok("Payroll processed",
                payrollService.processSingleEmployee(empId, month)));
    }

    @PutMapping("/{id}/mark-paid")
    public ResponseEntity<?> markPaid(@PathVariable Long id,
                                       @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
            ApiResponse.ok("Marked as PAID",
                payrollService.markAsPaid(id,
                    body.get("paymentDate"),
                    body.get("reference"))));
    }

    @GetMapping("/tax-preview")
    public ResponseEntity<?> taxPreview(@RequestParam BigDecimal salary) {
        return ResponseEntity.ok(
            ApiResponse.ok(payrollService.calculateTax(salary)));
    }
}
