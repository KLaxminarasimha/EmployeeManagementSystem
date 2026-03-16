package com.uniquehire.ems.service.interfaces;

import com.uniquehire.ems.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IEmployeeService {

    EmployeeResponse     createEmployee(EmployeeRequest request);
    EmployeeResponse     getEmployeeById(Long id);
    EmployeeResponse     getEmployeeByEmail(String email);
    EmployeeListResponse getAllEmployees(int page, int size, String dept, String status, String search);
    EmployeeResponse     updateEmployee(Long id, EmployeeRequest request);
    void                 deactivateEmployee(Long id);
    EmployeeResponse     changeStatus(Long id, String newStatus);
    String               uploadProfilePhoto(Long id, MultipartFile file);
    List<EmployeeResponse> getDirectReports(Long managerId);
    List<DashboardStatsResponse.DeptHeadcount> getDepartmentBreakdown();
    AttendanceSummaryResponse getAttendanceSummary(Long employeeId, String month);
}
