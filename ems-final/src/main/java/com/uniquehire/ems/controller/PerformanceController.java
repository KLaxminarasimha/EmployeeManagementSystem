package com.uniquehire.ems.controller;

import com.uniquehire.ems.dto.*;
import com.uniquehire.ems.service.interfaces.IPerformanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/performance")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class PerformanceController {

    private final IPerformanceService performanceService;

    @GetMapping
    public ResponseEntity<?> getByPeriod(
            @RequestParam(required = false) String period) {
        if (period != null)
            return ResponseEntity.ok(
                ApiResponse.ok(performanceService.getReviewsByPeriod(period)));
        return ResponseEntity.ok(
            ApiResponse.ok(performanceService.getPerformanceTrend()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.ok(performanceService.getReviewById(id)));
    }

    @GetMapping("/employee/{empId}/history")
    public ResponseEntity<?> getHistory(@PathVariable Long empId) {
        return ResponseEntity.ok(
            ApiResponse.ok(performanceService.getEmployeeHistory(empId)));
    }

    @GetMapping("/top-performers")
    public ResponseEntity<?> getTopPerformers(
            @RequestParam String period,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(
            ApiResponse.ok(performanceService.getTopPerformers(period, limit)));
    }

    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody PerformanceReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(performanceService.createReview(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                     @Valid @RequestBody PerformanceReviewRequest request) {
        return ResponseEntity.ok(
            ApiResponse.ok("Review updated",
                performanceService.updateReview(id, request)));
    }

    @PutMapping("/{id}/submit")
    public ResponseEntity<?> submit(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.ok("Review submitted",
                performanceService.submitReview(id)));
    }

    @PutMapping("/{id}/acknowledge")
    public ResponseEntity<?> acknowledge(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.ok("Review acknowledged",
                performanceService.acknowledgeReview(id)));
    }
}
