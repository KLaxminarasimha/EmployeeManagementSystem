package com.uniquehire.ems.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeRequest {
    @NotBlank @Size(max=100) private String firstName;
    @NotBlank @Size(max=100) private String lastName;
    @NotBlank @Email         private String email;
    @Pattern(regexp="^\\+?[0-9]{10,15}$", message="Invalid phone") private String phone;
    @NotBlank                private String designation;
    @NotNull                 private Long   departmentId;
    private String    employmentType;
    private String    workMode;
    @NotNull          private LocalDate   dateOfJoining;
    private LocalDate dateOfBirth;
    private String    address;
    @NotNull @Positive private BigDecimal annualSalary;
    private Long managerId;
}
