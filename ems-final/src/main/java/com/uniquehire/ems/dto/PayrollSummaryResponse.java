package com.uniquehire.ems.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PayrollSummaryResponse {
    private String     month;
    private BigDecimal totalGross;
    private BigDecimal totalDeductions;
    private BigDecimal totalNet;
    private int        totalEmployees;
    private int        processed;
    private int        paid;
    private int        draft;
    private List<MonthlyTrend> trend;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MonthlyTrend {
        private String     month;
        private BigDecimal totalNet;
    }
}
