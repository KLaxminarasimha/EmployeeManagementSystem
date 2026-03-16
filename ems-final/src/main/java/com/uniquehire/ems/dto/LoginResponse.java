package com.uniquehire.ems.dto;
import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginResponse {
    private String token;
    private String refreshToken;
    private String role;
    private Long   userId;
    private Long   employeeId;
    private String fullName;
    private long   expiresIn;
}
