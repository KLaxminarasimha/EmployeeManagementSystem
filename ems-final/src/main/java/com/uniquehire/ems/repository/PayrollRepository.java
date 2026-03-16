package com.uniquehire.ems.repository;

import com.uniquehire.ems.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    Optional<Payroll> findByEmployee_IdAndPayrollMonth(Long employeeId, LocalDate payrollMonth);

    boolean existsByEmployee_IdAndPayrollMonth(Long employeeId, LocalDate payrollMonth);

    List<Payroll> findByPayrollMonthOrderByEmployee_FirstNameAsc(LocalDate payrollMonth);

    List<Payroll> findByEmployee_IdOrderByPayrollMonthDesc(Long employeeId);

    @Query("""
        SELECT SUM(p.netSalary)
        FROM Payroll p
        WHERE p.payrollMonth = :month AND p.status != 'DRAFT'
        """)
    Optional<BigDecimal> getTotalNetForMonth(@Param("month") LocalDate month);

    @Query("""
        SELECT p.payrollMonth, SUM(p.netSalary)
        FROM Payroll p
        WHERE p.payrollMonth >= :from AND p.status != 'DRAFT'
        GROUP BY p.payrollMonth
        ORDER BY p.payrollMonth ASC
        """)
    List<Object[]> getMonthlyTrend(@Param("from") LocalDate from);

    @Query("""
        SELECT p.status, COUNT(p)
        FROM Payroll p
        WHERE p.payrollMonth = :month
        GROUP BY p.status
        """)
    List<Object[]> countByStatusForMonth(@Param("month") LocalDate month);
}
