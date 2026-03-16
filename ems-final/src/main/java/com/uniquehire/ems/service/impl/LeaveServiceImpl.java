package com.uniquehire.ems.service.impl;

import com.uniquehire.ems.dto.*;
import com.uniquehire.ems.entity.*;
import com.uniquehire.ems.exception.*;
import com.uniquehire.ems.kafka.*;
import com.uniquehire.ems.repository.*;
import com.uniquehire.ems.service.interfaces.ILeaveService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements ILeaveService {

    private static final Logger log = LoggerFactory.getLogger(LeaveServiceImpl.class);

    private final LeaveRequestRepository leaveRepo;
    private final LeaveTypeRepository    leaveTypeRepo;
    private final EmployeeRepository     employeeRepo;
    private final EmsKafkaProducer       kafkaProducer;

    // ── APPLY ────────────────────────────────────────────────

    @Override
    @Transactional
    public LeaveResponse applyLeave(LeaveRequestDto req) {
        Employee  emp = findEmpOrThrow(req.getEmployeeId());
        LeaveType lt  = findLeaveTypeOrThrow(req.getLeaveTypeId());

        if (req.getFromDate().isBefore(LocalDate.now()))
            throw new BusinessException("Cannot apply for past dates.");
        if (req.getFromDate().isAfter(req.getToDate()))
            throw new BusinessException("From date must be before or equal to to date.");
        if (leaveRepo.countOverlapping(req.getEmployeeId(), req.getFromDate(), req.getToDate()) > 0)
            throw new BusinessException("Dates overlap with an existing leave request.");

        int numDays = countWorkingDays(req.getFromDate(), req.getToDate());
        if (numDays == 0)
            throw new BusinessException("No working days in the selected date range.");
        if (emp.getLeaveBalance() < numDays)
            throw new BusinessException(String.format(
                "Insufficient balance. Available: %d, Requested: %d",
                emp.getLeaveBalance(), numDays));

        LeaveRequest lr = leaveRepo.save(LeaveRequest.builder()
            .employee(emp).leaveType(lt)
            .fromDate(req.getFromDate()).toDate(req.getToDate())
            .numDays(numDays).reason(req.getReason())
            .status(LeaveRequest.LeaveStatus.PENDING).build());

        kafkaProducer.publishLeaveRequested(buildEvent(lr, "LEAVE_REQUESTED"));
        log.info("Leave applied → id={} emp={} days={}", lr.getId(), emp.getFullName(), numDays);
        return toResponse(lr);
    }

    // ── APPROVE ──────────────────────────────────────────────

    @Override
    @Transactional
    public LeaveResponse approveLeave(Long leaveId, LeaveReviewRequest req) {
        LeaveRequest lr = findLeaveOrThrow(leaveId);
        if (lr.getStatus() != LeaveRequest.LeaveStatus.PENDING)
            throw new BusinessException("Only PENDING leaves can be approved. Status: " + lr.getStatus());

        Employee reviewer = findEmpOrThrow(req.getReviewerId());
        Employee emp      = lr.getEmployee();

        if (emp.getLeaveBalance() - lr.getNumDays() < 0)
            throw new BusinessException("Employee has insufficient leave balance.");

        emp.setLeaveBalance(emp.getLeaveBalance() - lr.getNumDays());
        employeeRepo.save(emp);

        lr.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        lr.setReviewedBy(reviewer);
        lr.setReviewedAt(ZonedDateTime.now());
        lr.setReviewComment(req.getComment());
        lr = leaveRepo.save(lr);

        kafkaProducer.publishLeaveApproved(buildEvent(lr, "LEAVE_APPROVED"));
        log.info("Leave APPROVED → id={} emp={}", leaveId, emp.getFullName());
        return toResponse(lr);
    }

    // ── REJECT ───────────────────────────────────────────────

    @Override
    @Transactional
    public LeaveResponse rejectLeave(Long leaveId, LeaveReviewRequest req) {
        LeaveRequest lr = findLeaveOrThrow(leaveId);
        if (lr.getStatus() != LeaveRequest.LeaveStatus.PENDING)
            throw new BusinessException("Only PENDING leaves can be rejected. Status: " + lr.getStatus());

        Employee reviewer = findEmpOrThrow(req.getReviewerId());
        lr.setStatus(LeaveRequest.LeaveStatus.REJECTED);
        lr.setReviewedBy(reviewer);
        lr.setReviewedAt(ZonedDateTime.now());
        lr.setReviewComment(req.getComment());
        lr = leaveRepo.save(lr);

        kafkaProducer.publishLeaveRejected(buildEvent(lr, "LEAVE_REJECTED"));
        log.info("Leave REJECTED → id={} emp={}", leaveId, lr.getEmployee().getFullName());
        return toResponse(lr);
    }

    // ── CANCEL ───────────────────────────────────────────────

    @Override
    @Transactional
    public LeaveResponse cancelLeave(Long leaveId) {
        LeaveRequest lr = findLeaveOrThrow(leaveId);
        if (lr.getStatus() == LeaveRequest.LeaveStatus.REJECTED)
            throw new BusinessException("Cannot cancel a rejected leave.");
        if (lr.getStatus() == LeaveRequest.LeaveStatus.CANCELLED)
            throw new BusinessException("Leave is already cancelled.");

        if (lr.getStatus() == LeaveRequest.LeaveStatus.APPROVED) {
            Employee emp = lr.getEmployee();
            emp.setLeaveBalance(emp.getLeaveBalance() + lr.getNumDays());
            employeeRepo.save(emp);
            log.info("Leave balance restored → +{} days for {}", lr.getNumDays(), emp.getFullName());
        }

        lr.setStatus(LeaveRequest.LeaveStatus.CANCELLED);
        return toResponse(leaveRepo.save(lr));
    }

    // ── GETTERS ──────────────────────────────────────────────

    @Override public LeaveResponse       getLeaveById(Long id)           { return toResponse(findLeaveOrThrow(id)); }

    @Override
    public List<LeaveResponse> getLeavesByEmployee(Long empId) {
        findEmpOrThrow(empId);
        return leaveRepo.findByEmployee_IdOrderByCreatedAtDesc(empId)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<LeaveResponse> getPendingLeaves() {
        return leaveRepo.findByStatusOrderByCreatedAtAsc(LeaveRequest.LeaveStatus.PENDING)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<LeaveResponse> getPendingLeavesForManager(Long managerId) {
        findEmpOrThrow(managerId);
        return leaveRepo.findPendingForManager(managerId)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<LeaveResponse> getLeavesByStatus(String status) {
        try {
            return leaveRepo.findByStatusOrderByCreatedAtAsc(
                LeaveRequest.LeaveStatus.valueOf(status.toUpperCase()))
                .stream().map(this::toResponse).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid status: " + status);
        }
    }

    @Override
    public LeaveBalanceResponse getLeaveBalance(Long empId) {
        Employee emp  = findEmpOrThrow(empId);
        int year      = LocalDate.now().getYear();
        List<Object[]> usage = leaveRepo.getLeaveUsageByType(empId, year);

        int usedDays = usage.stream().mapToInt(r -> ((Long) r[1]).intValue()).sum();
        int pendingDays = leaveRepo.findByEmployee_IdOrderByCreatedAtDesc(empId).stream()
            .filter(l -> l.getStatus() == LeaveRequest.LeaveStatus.PENDING)
            .mapToInt(LeaveRequest::getNumDays).sum();

        List<LeaveBalanceResponse.LeaveTypeBalance> breakdown = usage.stream()
            .map(r -> LeaveBalanceResponse.LeaveTypeBalance.builder()
                .leaveType((String) r[0])
                .used(((Long) r[1]).intValue())
                .remaining(emp.getLeaveBalance()).build())
            .collect(Collectors.toList());

        return LeaveBalanceResponse.builder()
            .employeeId(empId).employeeName(emp.getFullName())
            .totalEntitlement(18).usedDays(usedDays)
            .pendingDays(pendingDays).remainingDays(emp.getLeaveBalance())
            .breakdown(breakdown).build();
    }

    @Override
    public List<LeaveTypeResponse> getAllLeaveTypes() {
        return leaveTypeRepo.findAll().stream()
            .map(lt -> LeaveTypeResponse.builder()
                .id(lt.getId()).name(lt.getName())
                .annualQuota(lt.getAnnualQuota())
                .isPaid(lt.getIsPaid()).carryForward(lt.getCarryForward()).build())
            .collect(Collectors.toList());
    }

    // ── PRIVATE HELPERS ──────────────────────────────────────

    private int countWorkingDays(LocalDate from, LocalDate to) {
        int count = 0;
        LocalDate d = from;
        while (!d.isAfter(to)) {
            DayOfWeek dow = d.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) count++;
            d = d.plusDays(1);
        }
        return count;
    }

    private Employee    findEmpOrThrow(Long id)  { return employeeRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employee", id)); }
    private LeaveType   findLeaveTypeOrThrow(Long id) { return leaveTypeRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("LeaveType", id)); }
    private LeaveRequest findLeaveOrThrow(Long id) { return leaveRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", id)); }

    private LeaveRequestEvent buildEvent(LeaveRequest lr, String type) {
        return LeaveRequestEvent.builder()
            .leaveRequestId(lr.getId()).employeeId(lr.getEmployee().getId())
            .employeeName(lr.getEmployee().getFullName()).email(lr.getEmployee().getEmail())
            .managerEmail(lr.getEmployee().getManager() != null
                ? lr.getEmployee().getManager().getEmail() : "hr@uniquehire.co.in")
            .leaveType(lr.getLeaveType().getName())
            .fromDate(lr.getFromDate()).toDate(lr.getToDate())
            .numDays(lr.getNumDays()).reason(lr.getReason()).eventType(type).build();
    }

    // ── MAPPER ───────────────────────────────────────────────

    public LeaveResponse toResponse(LeaveRequest lr) {
        return LeaveResponse.builder()
            .id(lr.getId()).employeeId(lr.getEmployee().getId())
            .employeeName(lr.getEmployee().getFullName())
            .leaveType(lr.getLeaveType().getName())
            .fromDate(lr.getFromDate()).toDate(lr.getToDate()).numDays(lr.getNumDays())
            .reason(lr.getReason()).status(lr.getStatus().name())
            .reviewerName(lr.getReviewedBy() != null ? lr.getReviewedBy().getFullName() : null)
            .reviewedAt(lr.getReviewedAt()).reviewComment(lr.getReviewComment())
            .createdAt(lr.getCreatedAt()).build();
    }
}
