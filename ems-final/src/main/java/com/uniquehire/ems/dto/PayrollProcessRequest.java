package com.uniquehire.ems.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PayrollProcessRequest {
    @NotNull private LocalDate payrollMonth;
    @NotNull private Long      processedById;
}
