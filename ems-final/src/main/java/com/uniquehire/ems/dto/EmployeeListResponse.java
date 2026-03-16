package com.uniquehire.ems.dto;

import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeListResponse {
    private List<EmployeeResponse> employees;
    private long totalElements;
    private int  totalPages;
    private int  currentPage;
    private int  pageSize;
}
