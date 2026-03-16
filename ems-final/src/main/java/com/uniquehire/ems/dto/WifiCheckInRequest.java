package com.uniquehire.ems.dto;
import jakarta.validation.constraints.*;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WifiCheckInRequest {
    @NotNull  private Long   employeeId;
    @NotBlank private String ssid;
}
