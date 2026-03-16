package com.uniquehire.ems.kafka;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PayrollProcessedEvent {
    private Long   payrollId;
    private Long   employeeId;
    private String employeeName;
    private String email;
    private String payrollMonth;
    private Double netSalary;
    private String paymentDate;
    private String eventType = "PAYROLL_PROCESSED";
}
