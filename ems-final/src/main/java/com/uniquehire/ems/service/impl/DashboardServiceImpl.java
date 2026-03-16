package com.uniquehire.ems.service.impl;

import com.uniquehire.ems.dto.*;
import com.uniquehire.ems.entity.*;
import com.uniquehire.ems.repository.*;
import com.uniquehire.ems.service.interfaces.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private final EmployeeRepository          employeeRepo;
    private final AttendanceRepository        attendanceRepo;
    private final LeaveRequestRepository      leaveRepo;
    private final PayrollRepository           payrollRepo;
    private final PerformanceReviewRepository perfRepo;

    @Override
    public DashboardStatsResponse getDashboardStats() {
        log.debug("Aggregating dashboard stats");

        LocalDate today      = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        // ── Employee counts ──────────────────────────────────
        long totalEmployees  = employeeRepo.count();
        long activeEmployees = employeeRepo.countByStatus(Employee.EmployeeStatus.ACTIVE);

        // ── Today attendance ─────────────────────────────────
        List<Attendance> todayAll = attendanceRepo
            .findByAttendanceDateOrderByEmployee_FirstNameAsc(today);

        long inOffice = todayAll.stream()
            .filter(a -> a.getLocation() == Attendance.Location.OFFICE).count();
        long wfhToday = todayAll.stream()
            .filter(a -> a.getLocation() == Attendance.Location.WFH).count();

        // ── On leave today ───────────────────────────────────
        long onLeave = leaveRepo.findCurrentlyOnLeave(today).size();

        // ── Absent = active not yet marked and not on leave ──
        long absentToday = Math.max(0, activeEmployees - todayAll.size() - onLeave);

        // ── Pending leave requests ───────────────────────────
        long pendingLeaves = leaveRepo.countByStatus(LeaveRequest.LeaveStatus.PENDING);

        // ── Payroll this month ───────────────────────────────
        BigDecimal payrollThisMonth = payrollRepo
            .getTotalNetForMonth(monthStart).orElse(BigDecimal.ZERO);

        // ── Average performance score ────────────────────────
        List<Object[]> perfRows = perfRepo.getAverageScoreByPeriod();
        double avgPerf = perfRows.isEmpty() ? 0.0
            : ((BigDecimal) perfRows.get(perfRows.size() - 1)[1]).doubleValue();

        // ── New hires this month ─────────────────────────────
        long newHires = employeeRepo.countNewHiresSince(monthStart);

        // ── Department breakdown ─────────────────────────────
        List<DashboardStatsResponse.DeptHeadcount> deptBreakdown =
            employeeRepo.countByDepartment().stream()
                .map(r -> DashboardStatsResponse.DeptHeadcount.builder()
                    .department((String) r[0]).count((Long) r[1]).build())
                .collect(Collectors.toList());

        // ── 6-month payroll trend ────────────────────────────
        List<PayrollSummaryResponse.MonthlyTrend> payrollTrend =
            payrollRepo.getMonthlyTrend(monthStart.minusMonths(5)).stream()
                .map(r -> PayrollSummaryResponse.MonthlyTrend.builder()
                    .month(((LocalDate) r[0])
                        .format(DateTimeFormatter.ofPattern("MMM yyyy")))
                    .totalNet((BigDecimal) r[1]).build())
                .collect(Collectors.toList());

        return DashboardStatsResponse.builder()
            .totalEmployees((int) totalEmployees)
            .activeEmployees((int) activeEmployees)
            .inOfficeToday(inOffice)
            .wfhToday(wfhToday)
            .absentToday(absentToday)
            .onLeaveToday(onLeave)
            .pendingLeaveRequests(pendingLeaves)
            .payrollThisMonth(payrollThisMonth)
            .avgPerformanceScore(Math.round(avgPerf * 10.0) / 10.0)
            .newHiresThisMonth((int) newHires)
            .deptBreakdown(deptBreakdown)
            .payrollTrend(payrollTrend)
            .build();
    }
}
