package com.uniquehire.ems.controller;

import com.uniquehire.ems.dto.ApiResponse;
import com.uniquehire.ems.service.interfaces.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class DashboardController {

    private final IDashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(
            ApiResponse.ok(dashboardService.getDashboardStats()));
    }
}
