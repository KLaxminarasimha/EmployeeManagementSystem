package com.uniquehire.ems.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveBalanceResponse {
    private Long   employeeId;
    private String employeeName;
    private int    totalEntitlement;
    private int    usedDays;
    private int    pendingDays;
    private int    remainingDays;
    private List<LeaveTypeBalance> breakdown;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LeaveTypeBalance {
        private String leaveType;
        private int    entitled;
        private int    used;
        private int    pending;
        private int    remaining;
    }
}
