package com.uniquehire.ems.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardStatsResponse {
    private int        totalEmployees;
    private int        activeEmployees;
    private long       inOfficeToday;
    private long       wfhToday;
    private long       absentToday;
    private long       onLeaveToday;
    private long       pendingLeaveRequests;
    private BigDecimal payrollThisMonth;
    private double     avgPerformanceScore;
    private int        newHiresThisMonth;
    private List<DeptHeadcount>                     deptBreakdown;
    private List<PayrollSummaryResponse.MonthlyTrend> payrollTrend;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DeptHeadcount {
        private String department;
        private long   count;
    }
}
