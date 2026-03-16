package com.uniquehire.ems.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveRequestDto {
    @NotNull  private Long      employeeId;
    @NotNull  private Long      leaveTypeId;
    @NotNull  private LocalDate fromDate;
    @NotNull  private LocalDate toDate;
    @Size(max = 500) private String reason;
}
