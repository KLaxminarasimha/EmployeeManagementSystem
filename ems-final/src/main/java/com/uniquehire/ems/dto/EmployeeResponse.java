package com.uniquehire.ems.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeResponse {
    private Long   id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String designation;
    private String department;
    private Long   departmentId;
    private String employmentType;
    private String workMode;
    private String status;
    private LocalDate   dateOfJoining;
    private BigDecimal  annualSalary;
    private BigDecimal  monthlySalary;
    private Integer     leaveBalance;
    private String      managerName;
    private Long        managerId;
    private String      profilePhoto;
    private ZonedDateTime createdAt;
}
