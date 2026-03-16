package com.uniquehire.ems.dto;
import lombok.*;
import java.time.LocalDate;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TodayAttendanceStats {
    private LocalDate date;
    private long inOffice;
    private long wfh;
    private long absent;
    private long onLeave;
    private long total;
}
