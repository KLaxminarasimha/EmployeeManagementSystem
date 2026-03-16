package com.uniquehire.ems.service.interfaces;

import com.uniquehire.ems.dto.*;

import java.util.List;

public interface ILeaveService {

    LeaveResponse        applyLeave(LeaveRequestDto request);
    LeaveResponse        approveLeave(Long leaveId, LeaveReviewRequest request);
    LeaveResponse        rejectLeave(Long leaveId, LeaveReviewRequest request);
    LeaveResponse        cancelLeave(Long leaveId);
    LeaveResponse        getLeaveById(Long leaveId);
    List<LeaveResponse>  getLeavesByEmployee(Long employeeId);
    List<LeaveResponse>  getPendingLeaves();
    List<LeaveResponse>  getPendingLeavesForManager(Long managerId);
    List<LeaveResponse>  getLeavesByStatus(String status);
    LeaveBalanceResponse getLeaveBalance(Long employeeId);
    List<LeaveTypeResponse> getAllLeaveTypes();
}
