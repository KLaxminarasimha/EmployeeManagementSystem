package com.uniquehire.ems.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PerformanceTrendDto {
    private String     period;
    private BigDecimal avgScore;
}
