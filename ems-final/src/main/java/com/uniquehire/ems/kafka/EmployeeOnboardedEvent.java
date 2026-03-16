package com.uniquehire.ems.kafka;

import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeOnboardedEvent {
    private Long      employeeId;
    private String    employeeName;
    private String    email;
    private String    department;
    private String    designation;
    private LocalDate dateOfJoining;
    private String    eventType = "EMPLOYEE_ONBOARDED";
}
