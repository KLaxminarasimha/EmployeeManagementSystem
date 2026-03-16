package com.uniquehire.ems.dto;
import lombok.*;
import java.time.LocalDate;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WeeklyAttendanceDto {
    private LocalDate date;
    private String    dayName;
    private Long      officeCount;
    private Long      wfhCount;
}
