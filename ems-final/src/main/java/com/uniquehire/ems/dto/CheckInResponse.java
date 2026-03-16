package com.uniquehire.ems.dto;
import lombok.*;
import java.time.ZonedDateTime;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckInResponse {
    private boolean success;
    private Long employeeId;
    private String employeeName;
    private String markMethod;
    private String location;
    private ZonedDateTime checkInTime;
    private String message;
}
