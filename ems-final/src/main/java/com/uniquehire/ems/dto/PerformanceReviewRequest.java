package com.uniquehire.ems.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PerformanceReviewRequest {
    @NotNull  private Long       employeeId;
    @NotNull  private Long       reviewerId;
    @NotBlank private String     reviewPeriod;
    @NotNull  private LocalDate  reviewDate;
    @NotNull  @DecimalMin("0") @DecimalMax("100") private BigDecimal score;
    @Size(max = 1000) private String strengths;
    @Size(max = 1000) private String improvements;
    @Size(max = 1000) private String goalsNext;
}
