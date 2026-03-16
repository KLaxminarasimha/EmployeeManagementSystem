package com.uniquehire.ems.controller;

import com.uniquehire.ems.dto.ApiResponse;
import com.uniquehire.ems.dto.LoginRequest;
import com.uniquehire.ems.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AuthController {

    private final AuthServiceImpl authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
            ApiResponse.ok(authService.refreshToken(body.get("refreshToken"))));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, Object> body) {
        authService.changePassword(
            Long.valueOf(body.get("userId").toString()),
            body.get("oldPassword").toString(),
            body.get("newPassword").toString()
        );
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }
}
