package com.uniquehire.ems.service.interfaces;

import com.uniquehire.ems.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface IAttendanceService {

    CheckInResponse  checkInByQr(QrCheckInRequest request);
    CheckInResponse  checkInByWifi(WifiCheckInRequest request);
    CheckInResponse  checkInManual(Long employeeId, String location, String notes);
    CheckOutResponse checkOut(Long employeeId);
    byte[]           generateQrCodeImage() throws Exception;
    long             getCurrentTokenTtlMs();
    List<AttendanceResponse>  getTodayAttendanceLog();
    TodayAttendanceStats      getTodayStats();
    List<AttendanceResponse>  getHistory(Long employeeId, LocalDate from, LocalDate to);
    List<WeeklyAttendanceDto> getWeeklyData(LocalDate weekStart);
    AttendanceSummaryResponse getMonthlySummary(Long employeeId, String month);
    void autoMarkWfhEmployees();
}
