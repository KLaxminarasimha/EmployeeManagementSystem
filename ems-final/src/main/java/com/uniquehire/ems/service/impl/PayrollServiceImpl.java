package com.uniquehire.ems.service.impl;

import com.uniquehire.ems.dto.*;
import com.uniquehire.ems.entity.*;
import com.uniquehire.ems.exception.*;
import com.uniquehire.ems.kafka.*;
import com.uniquehire.ems.repository.*;
import com.uniquehire.ems.service.interfaces.IPayrollService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements IPayrollService {

    private static final Logger log = LoggerFactory.getLogger(PayrollServiceImpl.class);

    private final PayrollRepository    payrollRepo;
    private final EmployeeRepository   employeeRepo;
    private final AttendanceRepository attendanceRepo;
    private final EmsKafkaProducer     kafkaProducer;

    private static final BigDecimal HRA_PCT   = new BigDecimal("0.20");
    private static final BigDecimal ALLOW_PCT = new BigDecimal("0.10");
    private static final BigDecimal PF_PCT    = new BigDecimal("0.12");
    private static final BigDecimal PT_FLAT   = new BigDecimal("200");
    private static final BigDecimal CESS_PCT  = new BigDecimal("0.04");
    private static final BigDecimal ONE_LAKH  = new BigDecimal("100000");

    // ── BULK PROCESS ─────────────────────────────────────────

    @Override
    @Transactional
    public PayrollSummaryResponse bulkProcessPayroll(PayrollProcessRequest req) {
        LocalDate month = req.getPayrollMonth().withDayOfMonth(1);
        Employee  processor = employeeRepo.findById(req.getProcessedById())
            .orElseThrow(() -> new ResourceNotFoundException("Processor", req.getProcessedById()));

        List<Employee> actives = employeeRepo
            .findByStatus(Employee.EmployeeStatus.ACTIVE, Pageable.unpaged()).getContent();

        int processed = 0;
        BigDecimal totalNet = BigDecimal.ZERO;

        for (Employee emp : actives) {
            if (payrollRepo.existsByEmployee_IdAndPayrollMonth(emp.getId(), month)) continue;
            try {
                Payroll p = calculateAndSave(emp, month, processor);
                totalNet  = totalNet.add(p.getNetSalary());
                processed++;
                kafkaProducer.publishPayrollProcessed(PayrollProcessedEvent.builder()
                    .payrollId(p.getId()).employeeId(emp.getId())
                    .employeeName(emp.getFullName()).email(emp.getEmail())
                    .payrollMonth(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")))
                    .netSalary(p.getNetSalary().doubleValue())
                    .paymentDate(month.withDayOfMonth(28).toString()).build());
            } catch (Exception e) {
                log.error("Payroll failed for {}: {}", emp.getFullName(), e.getMessage());
            }
        }
        log.info("Bulk payroll done → processed={} totalNet={}", processed, totalNet);
        return buildSummary(month, processed, totalNet);
    }

    // ── SINGLE EMPLOYEE ──────────────────────────────────────

    @Override
    @Transactional
    public PayrollResponse processSingleEmployee(Long empId, String monthStr) {
        LocalDate month = LocalDate.parse(monthStr + "-01");
        Employee  emp   = employeeRepo.findById(empId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", empId));
        if (payrollRepo.existsByEmployee_IdAndPayrollMonth(empId, month))
            throw new BusinessException("Payroll already processed for this month.");
        Payroll p = calculateAndSave(emp, month, emp);
        kafkaProducer.publishPayrollProcessed(PayrollProcessedEvent.builder()
            .payrollId(p.getId()).employeeId(emp.getId())
            .employeeName(emp.getFullName()).email(emp.getEmail())
            .payrollMonth(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")))
            .netSalary(p.getNetSalary().doubleValue())
            .paymentDate(month.withDayOfMonth(28).toString()).build());
        return toResponse(p);
    }

    // ── CORE SALARY CALCULATION ───────────────────────────────

    private Payroll calculateAndSave(Employee emp, LocalDate month, Employee processor) {
        LocalDate from = month.withDayOfMonth(1);
        LocalDate to   = month.withDayOfMonth(month.lengthOfMonth());
        int wd         = countWorkingDays(from, to);
        long dw        = attendanceRepo.countDaysWorked(emp.getId(), from, to);
        if (dw == 0) dw = wd;

        BigDecimal basic  = emp.getMonthlySalary();
        BigDecimal perDay = basic.divide(BigDecimal.valueOf(wd), 4, RoundingMode.HALF_UP);
        BigDecimal leaveDed = perDay
            .multiply(BigDecimal.valueOf(Math.max(0, wd - dw)))
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal effBasic = basic.subtract(leaveDed)
            .max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        BigDecimal hra   = pct(effBasic, HRA_PCT);
        BigDecimal allow = pct(effBasic, ALLOW_PCT);
        BigDecimal gross = effBasic.add(hra).add(allow);

        BigDecimal pf    = pct(effBasic, PF_PCT);
        BigDecimal tax   = calculateAnnualIncomeTax(emp.getAnnualSalary())
            .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal totalDed = pf.add(PT_FLAT).add(tax);
        BigDecimal net      = gross.subtract(totalDed).setScale(2, RoundingMode.HALF_UP);

        return payrollRepo.save(Payroll.builder()
            .employee(emp).payrollMonth(month)
            .basicSalary(effBasic).hra(hra).specialAllowance(allow).grossSalary(gross)
            .pfDeduction(pf).professionalTax(PT_FLAT).incomeTax(tax)
            .totalDeductions(totalDed).netSalary(net)
            .workingDays(wd).daysWorked((int) dw).leaveDeduction(leaveDed)
            .status(Payroll.PayrollStatus.PROCESSED)
            .processedBy(processor).processedAt(ZonedDateTime.now())
            .paymentDate(month.withDayOfMonth(28)).build());
    }

    // ── TAX CALCULATION (New Regime FY 2025-26) ───────────────

    @Override
    public TaxBreakdownResponse calculateTax(BigDecimal salary) {
        BigDecimal stdDed  = new BigDecimal("75000");
        BigDecimal taxable = salary.subtract(stdDed).max(BigDecimal.ZERO);

        record Slab(BigDecimal from, BigDecimal to, BigDecimal rate, String label) {}
        List<Slab> slabs = List.of(
            new Slab(BigDecimal.ZERO, lakh(4),  new BigDecimal("0.00"), "₹0–₹4L @ 0%"),
            new Slab(lakh(4),  lakh(8),  new BigDecimal("0.05"), "₹4L–₹8L @ 5%"),
            new Slab(lakh(8),  lakh(12), new BigDecimal("0.10"), "₹8L–₹12L @ 10%"),
            new Slab(lakh(12), lakh(16), new BigDecimal("0.15"), "₹12L–₹16L @ 15%"),
            new Slab(lakh(16), lakh(20), new BigDecimal("0.20"), "₹16L–₹20L @ 20%"),
            new Slab(lakh(20), lakh(24), new BigDecimal("0.25"), "₹20L–₹24L @ 25%"),
            new Slab(lakh(24), new BigDecimal("99999999"), new BigDecimal("0.30"), "Above ₹24L @ 30%")
        );

        BigDecimal totalTax = BigDecimal.ZERO;
        List<TaxBreakdownResponse.SlabDetail> details = new ArrayList<>();

        for (Slab s : slabs) {
            if (taxable.compareTo(s.from()) > 0) {
                BigDecimal inSlab  = taxable.min(s.to()).subtract(s.from()).max(BigDecimal.ZERO);
                BigDecimal slabTax = inSlab.multiply(s.rate()).setScale(2, RoundingMode.HALF_UP);
                totalTax = totalTax.add(slabTax);
                if (slabTax.compareTo(BigDecimal.ZERO) > 0)
                    details.add(TaxBreakdownResponse.SlabDetail.builder()
                        .label(s.label()).taxableAmount(inSlab).taxAmount(slabTax).build());
            }
        }

        BigDecimal cess   = totalTax.multiply(CESS_PCT).setScale(2, RoundingMode.HALF_UP);
        BigDecimal annual = totalTax.add(cess);
        BigDecimal monthly = annual.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);

        return TaxBreakdownResponse.builder()
            .annualSalary(salary).standardDeduction(stdDed).taxableIncome(taxable)
            .incomeTaxBeforeCess(totalTax).cess(cess).totalAnnualTax(annual)
            .monthlyTax(monthly).slabs(details).build();
    }

    // ── GETTERS ──────────────────────────────────────────────

    @Override
    public List<PayrollResponse> getPayrollByMonth(String monthStr) {
        return payrollRepo.findByPayrollMonthOrderByEmployee_FirstNameAsc(
            LocalDate.parse(monthStr + "-01"))
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<PayrollResponse> getPayrollByEmployee(Long empId) {
        employeeRepo.findById(empId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", empId));
        return payrollRepo.findByEmployee_IdOrderByPayrollMonthDesc(empId)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public PayrollResponse getPayrollByEmployeeAndMonth(Long empId, String monthStr) {
        return payrollRepo.findByEmployee_IdAndPayrollMonth(
            empId, LocalDate.parse(monthStr + "-01"))
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Payroll not found for employee " + empId + " month " + monthStr));
    }

    @Override
    @Transactional
    public PayrollResponse markAsPaid(Long payrollId, String paymentDate, String reference) {
        Payroll p = payrollRepo.findById(payrollId)
            .orElseThrow(() -> new ResourceNotFoundException("Payroll", payrollId));
        if (p.getStatus() == Payroll.PayrollStatus.PAID)
            throw new BusinessException("Payroll is already marked as PAID.");
        if (p.getStatus() == Payroll.PayrollStatus.DRAFT)
            throw new BusinessException("Cannot mark DRAFT payroll as paid. Process it first.");
        p.setStatus(Payroll.PayrollStatus.PAID);
        p.setPaymentDate(LocalDate.parse(paymentDate));
        p.setPaymentReference(reference);
        return toResponse(payrollRepo.save(p));
    }

    @Override
    public PayrollSummaryResponse getPayrollSummary(String monthStr) {
        LocalDate month  = LocalDate.parse(monthStr + "-01");
        List<Payroll> all = payrollRepo.findByPayrollMonthOrderByEmployee_FirstNameAsc(month);
        BigDecimal net   = all.stream().map(Payroll::getNetSalary)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return buildSummary(month, all.size(), net);
    }

    // ── PRIVATE HELPERS ──────────────────────────────────────

    private BigDecimal calculateAnnualIncomeTax(BigDecimal salary) {
        return calculateTax(salary).getTotalAnnualTax();
    }

    private BigDecimal pct(BigDecimal base, BigDecimal rate) {
        return base.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal lakh(int n) {
        return ONE_LAKH.multiply(BigDecimal.valueOf(n));
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

    private PayrollSummaryResponse buildSummary(LocalDate month, int processed, BigDecimal total) {
        List<PayrollSummaryResponse.MonthlyTrend> trend = payrollRepo
            .getMonthlyTrend(month.minusMonths(5)).stream()
            .map(r -> PayrollSummaryResponse.MonthlyTrend.builder()
                .month(((LocalDate) r[0]).format(DateTimeFormatter.ofPattern("MMM yyyy")))
                .totalNet((BigDecimal) r[1]).build())
            .collect(Collectors.toList());
        return PayrollSummaryResponse.builder()
            .month(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")))
            .totalNet(total).totalEmployees(processed).processed(processed).trend(trend).build();
    }

    // ── MAPPER ───────────────────────────────────────────────

    public PayrollResponse toResponse(Payroll p) {
        return PayrollResponse.builder()
            .id(p.getId()).employeeId(p.getEmployee().getId())
            .employeeName(p.getEmployee().getFullName())
            .department(p.getEmployee().getDepartment() != null
                ? p.getEmployee().getDepartment().getName() : null)
            .payrollMonth(p.getPayrollMonth().format(DateTimeFormatter.ofPattern("MMMM yyyy")))
            .basicSalary(p.getBasicSalary()).hra(p.getHra()).specialAllowance(p.getSpecialAllowance())
            .grossSalary(p.getGrossSalary()).pfDeduction(p.getPfDeduction())
            .professionalTax(p.getProfessionalTax()).incomeTax(p.getIncomeTax())
            .totalDeductions(p.getTotalDeductions()).netSalary(p.getNetSalary())
            .workingDays(p.getWorkingDays()).daysWorked(p.getDaysWorked())
            .leaveDeduction(p.getLeaveDeduction()).status(p.getStatus().name())
            .paymentDate(p.getPaymentDate()).paymentReference(p.getPaymentReference())
            .processedAt(p.getProcessedAt()).build();
    }
}
