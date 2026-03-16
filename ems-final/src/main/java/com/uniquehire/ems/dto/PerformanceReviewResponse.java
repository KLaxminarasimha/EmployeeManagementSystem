package com.uniquehire.ems.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PerformanceReviewResponse {
    private Long          id;
    private Long          employeeId;
    private String        employeeName;
    private String        reviewerName;
    private String        reviewPeriod;
    private LocalDate     reviewDate;
    private BigDecimal    score;
    private String        rating;
    private String        strengths;
    private String        improvements;
    private String        goalsNext;
    private String        status;
    private ZonedDateTime createdAt;
}
