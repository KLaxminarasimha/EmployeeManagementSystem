package com.uniquehire.ems.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PayrollResponse {
    private Long          id;
    private Long          employeeId;
    private String        employeeName;
    private String        department;
    private String        payrollMonth;
    private BigDecimal    basicSalary;
    private BigDecimal    hra;
    private BigDecimal    specialAllowance;
    private BigDecimal    grossSalary;
    private BigDecimal    pfDeduction;
    private BigDecimal    professionalTax;
    private BigDecimal    incomeTax;
    private BigDecimal    totalDeductions;
    private BigDecimal    netSalary;
    private int           workingDays;
    private int           daysWorked;
    private BigDecimal    leaveDeduction;
    private String        status;
    private LocalDate     paymentDate;
    private String        paymentReference;
    private ZonedDateTime processedAt;
}
