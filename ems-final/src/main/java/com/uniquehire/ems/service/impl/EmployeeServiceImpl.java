package com.uniquehire.ems.service.impl;

import com.uniquehire.ems.dto.*;
import com.uniquehire.ems.entity.*;
import com.uniquehire.ems.exception.*;
import com.uniquehire.ems.kafka.*;
import com.uniquehire.ems.repository.*;
import com.uniquehire.ems.service.interfaces.IEmployeeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements IEmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository   employeeRepo;
    private final DepartmentRepository deptRepo;
    private final AttendanceRepository attendanceRepo;
    private final EmsKafkaProducer     kafkaProducer;

    // ── CREATE ───────────────────────────────────────────────

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest req) {
        log.info("Creating employee → {}", req.getEmail());

        if (employeeRepo.existsByEmail(req.getEmail()))
            throw new DuplicateResourceException("Email already registered: " + req.getEmail());

        Department dept = deptRepo.findById(req.getDepartmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Department", req.getDepartmentId()));

        Employee manager = null;
        if (req.getManagerId() != null) {
            manager = employeeRepo.findById(req.getManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager", req.getManagerId()));
        }

        Employee emp = Employee.builder()
            .employeeCode(generateNextCode())
            .firstName(req.getFirstName())
            .lastName(req.getLastName())
            .email(req.getEmail())
            .phone(req.getPhone())
            .designation(req.getDesignation())
            .department(dept)
            .employmentType(parseEnum(Employee.EmploymentType.class,
                req.getEmploymentType(), Employee.EmploymentType.FULL_TIME))
            .workMode(parseEnum(Employee.WorkMode.class,
                req.getWorkMode(), Employee.WorkMode.OFFICE))
            .status(Employee.EmployeeStatus.ACTIVE)
            .dateOfJoining(req.getDateOfJoining())
            .dateOfBirth(req.getDateOfBirth())
            .address(req.getAddress())
            .annualSalary(req.getAnnualSalary())
            .leaveBalance(18)
            .manager(manager)
            .build();

        emp = employeeRepo.save(emp);
        log.info("Employee created → {} ({})", emp.getFullName(), emp.getEmployeeCode());

        kafkaProducer.publishEmployeeOnboarded(EmployeeOnboardedEvent.builder()
            .employeeId(emp.getId())
            .employeeName(emp.getFullName())
            .email(emp.getEmail())
            .department(dept.getName())
            .designation(emp.getDesignation())
            .dateOfJoining(emp.getDateOfJoining())
            .build());

        return toResponse(emp);
    }

    // ── READ ─────────────────────────────────────────────────

    @Override
    public EmployeeResponse getEmployeeById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public EmployeeResponse getEmployeeByEmail(String email) {
        return toResponse(employeeRepo.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "email", email)));
    }

    @Override
    public EmployeeListResponse getAllEmployees(int page, int size,
                                                String dept, String status, String search) {
        Pageable pageable = PageRequest.of(page, size,
            Sort.by(Sort.Direction.ASC, "firstName", "lastName"));

        Page<Employee> result;

        if (search != null && !search.isBlank()) {
            result = employeeRepo.searchActive(search.trim(), pageable);
        } else if (dept != null && status != null) {
            result = employeeRepo.findByDepartment_NameAndStatus(
                dept, parseEnum(Employee.EmployeeStatus.class,
                    status, Employee.EmployeeStatus.ACTIVE), pageable);
        } else if (dept != null) {
            result = employeeRepo.findByDepartment_Name(dept, pageable);
        } else if (status != null) {
            result = employeeRepo.findByStatus(parseEnum(Employee.EmployeeStatus.class,
                status, Employee.EmployeeStatus.ACTIVE), pageable);
        } else {
            result = employeeRepo.findAll(pageable);
        }

        return EmployeeListResponse.builder()
            .employees(result.getContent().stream()
                .map(this::toResponse).collect(Collectors.toList()))
            .totalElements(result.getTotalElements())
            .totalPages(result.getTotalPages())
            .currentPage(page)
            .pageSize(size)
            .build();
    }

    // ── UPDATE ───────────────────────────────────────────────

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest req) {
        Employee emp = findOrThrow(id);

        if (!emp.getEmail().equalsIgnoreCase(req.getEmail())
                && employeeRepo.existsByEmail(req.getEmail()))
            throw new DuplicateResourceException("Email already in use: " + req.getEmail());

        Department dept = deptRepo.findById(req.getDepartmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Department", req.getDepartmentId()));

        emp.setFirstName(req.getFirstName());
        emp.setLastName(req.getLastName());
        emp.setEmail(req.getEmail());
        emp.setPhone(req.getPhone());
        emp.setDesignation(req.getDesignation());
        emp.setDepartment(dept);
        emp.setAnnualSalary(req.getAnnualSalary());
        emp.setDateOfBirth(req.getDateOfBirth());
        emp.setAddress(req.getAddress());

        if (req.getWorkMode() != null)
            emp.setWorkMode(parseEnum(Employee.WorkMode.class,
                req.getWorkMode(), emp.getWorkMode()));

        if (req.getEmploymentType() != null)
            emp.setEmploymentType(parseEnum(Employee.EmploymentType.class,
                req.getEmploymentType(), emp.getEmploymentType()));

        if (req.getManagerId() != null) {
            if (req.getManagerId().equals(id))
                throw new BusinessException("Employee cannot be their own manager.");
            emp.setManager(employeeRepo.findById(req.getManagerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager", req.getManagerId())));
        }

        return toResponse(employeeRepo.save(emp));
    }

    // ── DEACTIVATE ───────────────────────────────────────────

    @Override
    @Transactional
    public void deactivateEmployee(Long id) {
        Employee emp = findOrThrow(id);
        if (emp.getStatus() == Employee.EmployeeStatus.INACTIVE)
            throw new BusinessException("Employee is already inactive.");
        emp.setStatus(Employee.EmployeeStatus.INACTIVE);
        employeeRepo.save(emp);
        log.info("Employee deactivated → id={}", id);
    }

    // ── CHANGE STATUS ────────────────────────────────────────

    @Override
    @Transactional
    public EmployeeResponse changeStatus(Long id, String newStatus) {
        Employee emp = findOrThrow(id);
        try {
            emp.setStatus(Employee.EmployeeStatus.valueOf(newStatus.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid status: " + newStatus
                + ". Valid values: ACTIVE, INACTIVE, TERMINATED");
        }
        return toResponse(employeeRepo.save(emp));
    }

    // ── UPLOAD PHOTO ─────────────────────────────────────────

    @Override
    @Transactional
    public String uploadProfilePhoto(Long id, MultipartFile file) {
        Employee emp = findOrThrow(id);

        if (file.getContentType() == null || !file.getContentType().startsWith("image/"))
            throw new BusinessException("Only image files are allowed.");
        if (file.getSize() > 5 * 1024 * 1024)
            throw new BusinessException("Photo must be under 5 MB.");

        try {
            String dir  = "uploads/profiles/";
            Files.createDirectories(Paths.get(dir));
            String name = "EMP_" + id + "_" + System.currentTimeMillis()
                + getExtension(file.getOriginalFilename());
            Files.copy(file.getInputStream(), Paths.get(dir + name),
                StandardCopyOption.REPLACE_EXISTING);
            emp.setProfilePhoto(dir + name);
            employeeRepo.save(emp);
            return emp.getProfilePhoto();
        } catch (IOException e) {
            throw new BusinessException("Upload failed: " + e.getMessage());
        }
    }

    // ── DIRECT REPORTS ───────────────────────────────────────

    @Override
    public List<EmployeeResponse> getDirectReports(Long managerId) {
        findOrThrow(managerId);
        return employeeRepo.findActiveDirectReports(managerId)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── DEPT BREAKDOWN ───────────────────────────────────────

    @Override
    public List<DashboardStatsResponse.DeptHeadcount> getDepartmentBreakdown() {
        return employeeRepo.countByDepartment().stream()
            .map(r -> DashboardStatsResponse.DeptHeadcount.builder()
                .department((String) r[0]).count((Long) r[1]).build())
            .collect(Collectors.toList());
    }

    // ── ATTENDANCE SUMMARY ───────────────────────────────────

    @Override
    public AttendanceSummaryResponse getAttendanceSummary(Long employeeId, String month) {
        Employee  emp  = findOrThrow(employeeId);
        LocalDate from = LocalDate.parse(month + "-01");
        LocalDate to   = from.withDayOfMonth(from.lengthOfMonth());

        long daysWorked  = attendanceRepo.countDaysWorked(employeeId, from, to);
        int  workingDays = countWorkingDays(from, to);

        List<Attendance> history = attendanceRepo
            .findByEmployee_IdAndAttendanceDateBetweenOrderByAttendanceDateAsc(employeeId, from, to);

        int wfh  = (int) history.stream()
            .filter(a -> a.getLocation() == Attendance.Location.WFH).count();
        int half = (int) history.stream()
            .filter(a -> a.getStatus() == Attendance.AttendanceStatus.HALF_DAY).count();

        BigDecimal avgHours = attendanceRepo.avgWorkHours(employeeId, from, to);
        double pct = workingDays > 0
            ? Math.round(daysWorked * 1000.0 / workingDays) / 10.0 : 0.0;

        return AttendanceSummaryResponse.builder()
            .employeeId(employeeId)
            .employeeName(emp.getFullName())
            .month(month)
            .totalWorkingDays(workingDays)
            .daysPresent((int) daysWorked)
            .daysAbsent(workingDays - (int) daysWorked)
            .daysWfh(wfh)
            .halfDays(half)
            .avgWorkHours(avgHours)
            .attendancePercentage(pct)
            .build();
    }

    // ── PRIVATE HELPERS ──────────────────────────────────────

    private Employee findOrThrow(Long id) {
        return employeeRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }

    private String generateNextCode() {
        return employeeRepo.findMaxEmployeeCode()
            .map(last -> String.format("UH-%04d",
                Integer.parseInt(last.replace("UH-", "").trim()) + 1))
            .orElse("UH-0001");
    }

    private <T extends Enum<T>> T parseEnum(Class<T> clazz, String value, T defaultVal) {
        if (value == null || value.isBlank()) return defaultVal;
        try { return Enum.valueOf(clazz, value.toUpperCase().trim()); }
        catch (IllegalArgumentException e) { return defaultVal; }
    }

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

    private String getExtension(String filename) {
        return (filename != null && filename.contains("."))
            ? filename.substring(filename.lastIndexOf(".")) : ".jpg";
    }

    // ── MAPPER ───────────────────────────────────────────────

    public EmployeeResponse toResponse(Employee e) {
        return EmployeeResponse.builder()
            .id(e.getId())
            .employeeCode(e.getEmployeeCode())
            .firstName(e.getFirstName())
            .lastName(e.getLastName())
            .fullName(e.getFullName())
            .email(e.getEmail())
            .phone(e.getPhone())
            .designation(e.getDesignation())
            .department(e.getDepartment() != null ? e.getDepartment().getName() : null)
            .departmentId(e.getDepartment() != null ? e.getDepartment().getId()  : null)
            .employmentType(e.getEmploymentType() != null ? e.getEmploymentType().name() : null)
            .workMode(e.getWorkMode()       != null ? e.getWorkMode().name()       : null)
            .status(e.getStatus().name())
            .dateOfJoining(e.getDateOfJoining())
            .annualSalary(e.getAnnualSalary())
            .monthlySalary(e.getMonthlySalary())
            .leaveBalance(e.getLeaveBalance())
            .managerName(e.getManager() != null ? e.getManager().getFullName() : null)
            .managerId(e.getManager()   != null ? e.getManager().getId()       : null)
            .profilePhoto(e.getProfilePhoto())
            .createdAt(e.getCreatedAt())
            .build();
    }
}
