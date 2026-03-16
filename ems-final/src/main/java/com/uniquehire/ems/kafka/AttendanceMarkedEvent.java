package com.uniquehire.ems.kafka;

import lombok.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AttendanceMarkedEvent {
    private Long          employeeId;
    private String        employeeName;
    private String        email;
    private LocalDate     attendanceDate;
    private String        markMethod;
    private String        location;
    private ZonedDateTime checkIn;
    private String        eventType = "ATTENDANCE_MARKED";
}
