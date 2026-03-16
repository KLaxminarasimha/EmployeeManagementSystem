package com.uniquehire.ems.service.impl;

import com.uniquehire.ems.dto.*;
import com.uniquehire.ems.entity.*;
import com.uniquehire.ems.exception.*;
import com.uniquehire.ems.kafka.*;
import com.uniquehire.ems.repository.*;
import com.uniquehire.ems.service.interfaces.IAttendanceService;
import com.uniquehire.ems.service.interfaces.IQrTokenService;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements IAttendanceService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceServiceImpl.class);

    private final AttendanceRepository attendanceRepo;
    private final EmployeeRepository   employeeRepo;
    private final IQrTokenService      qrTokenService;
    private final EmsKafkaProducer     kafkaProducer;

    @Value("${app.qr.company-wifi-ssids}")
    private String companyWifiSsidsRaw;
    private Set<String> companyWifiSsids;

    @PostConstruct
    public void init() {
        companyWifiSsids = Arrays.stream(companyWifiSsidsRaw.split(","))
            .map(String::trim).collect(Collectors.toSet());
        log.info("Company WiFi SSIDs: {}", companyWifiSsids);
    }

    // ── QR CHECK-IN ──────────────────────────────────────────

    @Override
    @Transactional
    public CheckInResponse checkInByQr(QrCheckInRequest request) {
        log.info("QR check-in → empId={}", request.getEmployeeId());

        if (!qrTokenService.isTokenValid(request.getToken()))
            throw new BusinessException("QR token invalid or expired. Scan the latest code.");

        Employee  emp   = findEmpOrThrow(request.getEmployeeId());
        LocalDate today = LocalDate.now();

        if (attendanceRepo.existsByEmployee_IdAndAttendanceDate(emp.getId(), today)) {
            Attendance ex = attendanceRepo
                .findByEmployee_IdAndAttendanceDate(emp.getId(), today).get();
            throw new BusinessException("Already checked in today at " + ex.getCheckIn());
        }

        qrTokenService.invalidateToken(request.getToken());

        ZonedDateTime now = ZonedDateTime.now();
        attendanceRepo.save(Attendance.builder()
            .employee(emp).attendanceDate(today).checkIn(now)
            .status(Attendance.AttendanceStatus.PRESENT)
            .markMethod(Attendance.MarkMethod.QR)
            .location(Attendance.Location.OFFICE)
            .qrTokenUsed(request.getToken())
            .build());

        publishEvent(emp, "QR", "OFFICE", now);

        return CheckInResponse.builder()
            .success(true).employeeId(emp.getId()).employeeName(emp.getFullName())
            .markMethod("QR").location("OFFICE").checkInTime(now)
            .message("Check-in via QR successful! Have a great day.").build();
    }

    // ── WIFI CHECK-IN ────────────────────────────────────────

    @Override
    @Transactional
    public CheckInResponse checkInByWifi(WifiCheckInRequest request) {
        log.info("WiFi check-in → empId={} ssid={}", request.getEmployeeId(), request.getSsid());

        Employee  emp   = findEmpOrThrow(request.getEmployeeId());
        LocalDate today = LocalDate.now();

        // If already marked, return silently (mobile app calls this on every open)
        if (attendanceRepo.existsByEmployee_IdAndAttendanceDate(emp.getId(), today)) {
            Attendance ex = attendanceRepo
                .findByEmployee_IdAndAttendanceDate(emp.getId(), today).get();
            return CheckInResponse.builder()
                .success(true).employeeId(emp.getId()).employeeName(emp.getFullName())
                .markMethod(ex.getMarkMethod().name()).location(ex.getLocation().name())
                .checkInTime(ex.getCheckIn()).message("Already marked today.").build();
        }

        boolean onCompanyWifi = companyWifiSsids.contains(request.getSsid());
        Attendance.Location   loc = onCompanyWifi
            ? Attendance.Location.OFFICE : Attendance.Location.WFH;
        Attendance.MarkMethod mtd = onCompanyWifi
            ? Attendance.MarkMethod.WIFI_AUTO : Attendance.MarkMethod.WFH_AUTO;

        ZonedDateTime now = ZonedDateTime.now();
        attendanceRepo.save(Attendance.builder()
            .employee(emp).attendanceDate(today).checkIn(now)
            .status(Attendance.AttendanceStatus.PRESENT)
            .markMethod(mtd).location(loc).wifiSsid(request.getSsid())
            .build());

        publishEvent(emp, mtd.name(), loc.name(), now);

        String msg = onCompanyWifi
            ? "Auto check-in at office via WiFi."
            : "Work from Home marked automatically.";

        return CheckInResponse.builder()
            .success(true).employeeId(emp.getId()).employeeName(emp.getFullName())
            .markMethod(mtd.name()).location(loc.name()).checkInTime(now).message(msg).build();
    }

    // ── MANUAL CHECK-IN ──────────────────────────────────────

    @Override
    @Transactional
    public CheckInResponse checkInManual(Long empId, String locationStr, String notes) {
        Employee  emp   = findEmpOrThrow(empId);
        LocalDate today = LocalDate.now();

        if (attendanceRepo.existsByEmployee_IdAndAttendanceDate(empId, today))
            throw new BusinessException("Attendance already marked for today.");

        Attendance.Location loc = Attendance.Location.OFFICE;
        try { if (locationStr != null)
            loc = Attendance.Location.valueOf(locationStr.toUpperCase());
        } catch (IllegalArgumentException ignored) {}

        ZonedDateTime now = ZonedDateTime.now();
        attendanceRepo.save(Attendance.builder()
            .employee(emp).attendanceDate(today).checkIn(now)
            .status(Attendance.AttendanceStatus.PRESENT)
            .markMethod(Attendance.MarkMethod.MANUAL).location(loc).notes(notes)
            .build());

        publishEvent(emp, "MANUAL", loc.name(), now);

        return CheckInResponse.builder()
            .success(true).employeeId(empId).employeeName(emp.getFullName())
            .markMethod("MANUAL").location(loc.name()).checkInTime(now)
            .message("Manual check-in recorded.").build();
    }

    // ── CHECK-OUT ────────────────────────────────────────────

    @Override
    @Transactional
    public CheckOutResponse checkOut(Long empId) {
        findEmpOrThrow(empId);

        Attendance att = attendanceRepo
            .findByEmployee_IdAndAttendanceDate(empId, LocalDate.now())
            .orElseThrow(() -> new BusinessException("No check-in found for today."));

        if (att.getCheckOut() != null)
            throw new BusinessException("Already checked out at " + att.getCheckOut());

        ZonedDateTime now     = ZonedDateTime.now();
        long          minutes = ChronoUnit.MINUTES.between(att.getCheckIn(), now);
        BigDecimal    hours   = BigDecimal.valueOf(minutes)
            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        att.setCheckOut(now);
        att.setWorkHours(hours);
        if (hours.compareTo(BigDecimal.valueOf(4.5)) < 0)
            att.setStatus(Attendance.AttendanceStatus.HALF_DAY);

        attendanceRepo.save(att);

        return CheckOutResponse.builder()
            .success(true).employeeId(empId)
            .checkInTime(att.getCheckIn()).checkOutTime(now).workHours(hours)
            .message(String.format("Check-out recorded. Hours worked: %.2f", hours)).build();
    }

    // ── QR CODE IMAGE ────────────────────────────────────────

    @Override public byte[] generateQrCodeImage() throws Exception { return qrTokenService.generateQrCodeImage(); }
    @Override public long   getCurrentTokenTtlMs()                  { return qrTokenService.getCurrentTokenTtlMs(); }

    // ── TODAY LOG ────────────────────────────────────────────

    @Override
    public List<AttendanceResponse> getTodayAttendanceLog() {
        return attendanceRepo
            .findByAttendanceDateOrderByEmployee_FirstNameAsc(LocalDate.now())
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── TODAY STATS ──────────────────────────────────────────

    @Override
    public TodayAttendanceStats getTodayStats() {
        LocalDate today  = LocalDate.now();
        List<Attendance> all = attendanceRepo
            .findByAttendanceDateOrderByEmployee_FirstNameAsc(today);
        long inOffice = all.stream()
            .filter(a -> a.getLocation() == Attendance.Location.OFFICE).count();
        long wfh = all.stream()
            .filter(a -> a.getLocation() == Attendance.Location.WFH).count();
        long total = employeeRepo.countByStatus(Employee.EmployeeStatus.ACTIVE);
        return TodayAttendanceStats.builder()
            .date(today).inOffice(inOffice).wfh(wfh)
            .absent(total - all.size()).onLeave(Math.max(0, total - all.size())).total(total)
            .build();
    }

    // ── HISTORY ──────────────────────────────────────────────

    @Override
    public List<AttendanceResponse> getHistory(Long empId, LocalDate from, LocalDate to) {
        findEmpOrThrow(empId);
        if (from.isAfter(to)) throw new BusinessException("From date must be before to date.");
        return attendanceRepo
            .findByEmployee_IdAndAttendanceDateBetweenOrderByAttendanceDateAsc(empId, from, to)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── WEEKLY DATA ──────────────────────────────────────────

    @Override
    public List<WeeklyAttendanceDto> getWeeklyData(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        Map<LocalDate, WeeklyAttendanceDto> map = new LinkedHashMap<>();
        for (LocalDate d = weekStart; !d.isAfter(weekEnd); d = d.plusDays(1))
            map.put(d, WeeklyAttendanceDto.builder()
                .date(d).dayName(d.getDayOfWeek().name().substring(0, 3))
                .officeCount(0L).wfhCount(0L).build());

        for (Object[] r : attendanceRepo.getWeeklyByLocation(weekStart, weekEnd)) {
            LocalDate date = (LocalDate) r[0];
            String    loc  = r[1].toString();
            long      cnt  = (Long) r[2];
            if (map.containsKey(date)) {
                WeeklyAttendanceDto dto = map.get(date);
                if ("OFFICE".equals(loc)) dto.setOfficeCount(cnt);
                if ("WFH".equals(loc))    dto.setWfhCount(cnt);
            }
        }
        return new ArrayList<>(map.values());
    }

    // ── MONTHLY SUMMARY ──────────────────────────────────────

    @Override
    public AttendanceSummaryResponse getMonthlySummary(Long empId, String month) {
        Employee  emp  = findEmpOrThrow(empId);
        LocalDate from = LocalDate.parse(month + "-01");
        LocalDate to   = from.withDayOfMonth(from.lengthOfMonth());
        int       wd   = countWorkingDays(from, to);
        long      dw   = attendanceRepo.countDaysWorked(empId, from, to);

        List<Attendance> hist = attendanceRepo
            .findByEmployee_IdAndAttendanceDateBetweenOrderByAttendanceDateAsc(empId, from, to);
        int wfh  = (int) hist.stream().filter(a -> a.getLocation() == Attendance.Location.WFH).count();
        int half = (int) hist.stream()
            .filter(a -> a.getStatus() == Attendance.AttendanceStatus.HALF_DAY).count();

        return AttendanceSummaryResponse.builder()
            .employeeId(empId).employeeName(emp.getFullName()).month(month)
            .totalWorkingDays(wd).daysPresent((int) dw).daysAbsent(wd - (int) dw)
            .daysWfh(wfh).halfDays(half)
            .avgWorkHours(attendanceRepo.avgWorkHours(empId, from, to))
            .attendancePercentage(wd > 0 ? Math.round(dw * 1000.0 / wd) / 10.0 : 0.0)
            .build();
    }

    // ── SCHEDULED AUTO-MARK WFH ──────────────────────────────

    @Override
    @Scheduled(cron = "0 0 9 * * MON-FRI")
    @Transactional
    public void autoMarkWfhEmployees() {
        LocalDate today = LocalDate.now();
        List<Employee> wfhEmps = employeeRepo.findByWorkModeAndStatus(
            Employee.WorkMode.WFH, Employee.EmployeeStatus.ACTIVE);
        int marked = 0;
        for (Employee emp : wfhEmps) {
            if (!attendanceRepo.existsByEmployee_IdAndAttendanceDate(emp.getId(), today)) {
                ZonedDateTime now = ZonedDateTime.now();
                attendanceRepo.save(Attendance.builder()
                    .employee(emp).attendanceDate(today).checkIn(now)
                    .status(Attendance.AttendanceStatus.PRESENT)
                    .markMethod(Attendance.MarkMethod.WFH_AUTO)
                    .location(Attendance.Location.WFH)
                    .notes("Auto-marked: permanent WFH employee").build());
                publishEvent(emp, "WFH_AUTO", "WFH", now);
                marked++;
            }
        }
        log.info("Auto WFH mark complete: {} employees", marked);
    }

    // ── PRIVATE HELPERS ──────────────────────────────────────

    private Employee findEmpOrThrow(Long id) {
        return employeeRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }

    private void publishEvent(Employee emp, String method, String location, ZonedDateTime ci) {
        kafkaProducer.publishAttendanceMarked(AttendanceMarkedEvent.builder()
            .employeeId(emp.getId()).employeeName(emp.getFullName()).email(emp.getEmail())
            .attendanceDate(ci.toLocalDate()).markMethod(method).location(location).checkIn(ci)
            .build());
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

    // ── MAPPER ───────────────────────────────────────────────

    public AttendanceResponse toResponse(Attendance a) {
        return AttendanceResponse.builder()
            .id(a.getId()).employeeId(a.getEmployee().getId())
            .employeeName(a.getEmployee().getFullName())
            .attendanceDate(a.getAttendanceDate())
            .checkIn(a.getCheckIn()).checkOut(a.getCheckOut()).workHours(a.getWorkHours())
            .status(a.getStatus().name()).markMethod(a.getMarkMethod().name())
            .location(a.getLocation().name()).wifiSsid(a.getWifiSsid())
            .build();
    }
}
