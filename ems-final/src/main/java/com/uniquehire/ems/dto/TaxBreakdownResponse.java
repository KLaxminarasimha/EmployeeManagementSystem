package com.uniquehire.ems.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TaxBreakdownResponse {
    private BigDecimal annualSalary;
    private BigDecimal standardDeduction;
    private BigDecimal taxableIncome;
    private BigDecimal incomeTaxBeforeCess;
    private BigDecimal cess;
    private BigDecimal totalAnnualTax;
    private BigDecimal monthlyTax;
    private List<SlabDetail> slabs;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SlabDetail {
        private String     label;
        private BigDecimal taxableAmount;
        private BigDecimal taxAmount;
    }
}
