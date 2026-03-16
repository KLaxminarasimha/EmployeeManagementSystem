package com.uniquehire.ems.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AttendanceSummaryResponse {
    private Long       employeeId;
    private String     employeeName;
    private String     month;
    private int        totalWorkingDays;
    private int        daysPresent;
    private int        daysAbsent;
    private int        daysWfh;
    private int        halfDays;
    private int        holidays;
    private BigDecimal avgWorkHours;
    private double     attendancePercentage;
}
