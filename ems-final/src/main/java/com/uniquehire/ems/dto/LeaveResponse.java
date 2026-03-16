package com.uniquehire.ems.dto;

import lombok.*;
import java.time.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveResponse {
    private Long          id;
    private Long          employeeId;
    private String        employeeName;
    private String        leaveType;
    private LocalDate     fromDate;
    private LocalDate     toDate;
    private int           numDays;
    private String        reason;
    private String        status;
    private String        reviewerName;
    private ZonedDateTime reviewedAt;
    private String        reviewComment;
    private ZonedDateTime createdAt;
}
