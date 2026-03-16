package com.uniquehire.ems.dto;
import lombok.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CheckOutResponse {
    private boolean success;
    private Long employeeId;
    private ZonedDateTime checkInTime;
    private ZonedDateTime checkOutTime;
    private BigDecimal workHours;
    private String message;
}
