package com.uniquehire.ems.kafka;

import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveRequestEvent {
    private Long      leaveRequestId;
    private Long      employeeId;
    private String    employeeName;
    private String    email;
    private String    managerEmail;
    private String    leaveType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Integer   numDays;
    private String    reason;
    private String    eventType;
}
