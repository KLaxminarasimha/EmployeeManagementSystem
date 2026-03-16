package com.uniquehire.ems.controller;

import com.uniquehire.ems.dto.*;
import com.uniquehire.ems.service.interfaces.ILeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class LeaveController {

    private final ILeaveService leaveService;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long employeeId) {
        if (employeeId != null)
            return ResponseEntity.ok(
                ApiResponse.ok(leaveService.getLeavesByEmployee(employeeId)));
        if (status != null)
            return ResponseEntity.ok(
                ApiResponse.ok(leaveService.getLeavesByStatus(status)));
        return ResponseEntity.ok(ApiResponse.ok(leaveService.getPendingLeaves()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(leaveService.getLeaveById(id)));
    }

    @PostMapping
    public ResponseEntity<?> apply(@Valid @RequestBody LeaveRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(leaveService.applyLeave(request)));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id,
                                      @RequestBody LeaveReviewRequest request) {
        return ResponseEntity.ok(
            ApiResponse.ok("Leave approved", leaveService.approveLeave(id, request)));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id,
                                     @RequestBody LeaveReviewRequest request) {
        return ResponseEntity.ok(
            ApiResponse.ok("Leave rejected", leaveService.rejectLeave(id, request)));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.ok("Leave cancelled", leaveService.cancelLeave(id)));
    }

    @GetMapping("/{employeeId}/balance")
    public ResponseEntity<?> getBalance(@PathVariable Long employeeId) {
        return ResponseEntity.ok(
            ApiResponse.ok(leaveService.getLeaveBalance(employeeId)));
    }

    @GetMapping("/types")
    public ResponseEntity<?> getTypes() {
        return ResponseEntity.ok(ApiResponse.ok(leaveService.getAllLeaveTypes()));
    }

    @GetMapping("/pending/manager/{managerId}")
    public ResponseEntity<?> getPendingForManager(@PathVariable Long managerId) {
        return ResponseEntity.ok(
            ApiResponse.ok(leaveService.getPendingLeavesForManager(managerId)));
    }
}
