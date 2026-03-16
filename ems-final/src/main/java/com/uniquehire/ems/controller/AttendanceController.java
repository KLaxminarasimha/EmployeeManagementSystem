package com.uniquehire.ems.controller;

import com.uniquehire.ems.dto.*;
import com.uniquehire.ems.service.interfaces.IAttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AttendanceController {

    private final IAttendanceService attendanceService;

    @PostMapping("/checkin/qr")
    public ResponseEntity<?> checkInByQr(@Valid @RequestBody QrCheckInRequest request) {
        return ResponseEntity.ok(
            ApiResponse.ok(attendanceService.checkInByQr(request)));
    }

    @PostMapping("/checkin/wifi")
    public ResponseEntity<?> checkInByWifi(@Valid @RequestBody WifiCheckInRequest request) {
        return ResponseEntity.ok(
            ApiResponse.ok(attendanceService.checkInByWifi(request)));
    }

    @PostMapping("/checkin/manual")
    public ResponseEntity<?> checkInManual(@RequestBody Map<String, Object> body) {
        Long   empId    = Long.valueOf(body.get("employeeId").toString());
        String location = body.getOrDefault("location", "OFFICE").toString();
        String notes    = body.getOrDefault("notes", "").toString();
        return ResponseEntity.ok(
            ApiResponse.ok(attendanceService.checkInManual(empId, location, notes)));
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkOut(@RequestBody Map<String, Object> body) {
        Long empId = Long.valueOf(body.get("employeeId").toString());
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.checkOut(empId)));
    }

    @GetMapping(value = "/qr-code", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode() {
        try {
            byte[] image = attendanceService.generateQrCodeImage();
            return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("X-Token-Ttl-Ms",
                    String.valueOf(attendanceService.getCurrentTokenTtlMs()))
                .body(image);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/today")
    public ResponseEntity<?> getToday() {
        return ResponseEntity.ok(
            ApiResponse.ok(attendanceService.getTodayAttendanceLog()));
    }

    @GetMapping("/today/stats")
    public ResponseEntity<?> getTodayStats() {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getTodayStats()));
    }

    @GetMapping("/weekly")
    public ResponseEntity<?> getWeekly(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        if (weekStart == null)
            weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        return ResponseEntity.ok(
            ApiResponse.ok(attendanceService.getWeeklyData(weekStart)));
    }

    @GetMapping
    public ResponseEntity<?> getHistory(
            @RequestParam(required = false) Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(
            ApiResponse.ok(attendanceService.getHistory(employeeId, from, to)));
    }

    @GetMapping("/summary/{employeeId}")
    public ResponseEntity<?> getMonthlySummary(@PathVariable Long employeeId,
                                                @RequestParam String month) {
        return ResponseEntity.ok(
            ApiResponse.ok(attendanceService.getMonthlySummary(employeeId, month)));
    }
}
