package com.uniquehire.ems.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AttendanceResponse {
    private Long   id;
    private Long   employeeId;
    private String employeeName;
    private LocalDate     attendanceDate;
    private ZonedDateTime checkIn;
    private ZonedDateTime checkOut;
    private BigDecimal    workHours;
    private String status;
    private String markMethod;
    private String location;
    private String wifiSsid;
}
