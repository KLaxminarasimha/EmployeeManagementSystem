package com.uniquehire.ems.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveTypeResponse {
    private Long    id;
    private String  name;
    private Integer annualQuota;
    private Boolean isPaid;
    private Boolean carryForward;
}
