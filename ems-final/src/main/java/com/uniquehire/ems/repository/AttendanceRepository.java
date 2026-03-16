package com.uniquehire.ems.repository;

import com.uniquehire.ems.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployee_IdAndAttendanceDate(Long employeeId, LocalDate date);

    boolean existsByEmployee_IdAndAttendanceDate(Long employeeId, LocalDate date);

    List<Attendance> findByEmployee_IdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
            Long employeeId, LocalDate from, LocalDate to);

    List<Attendance> findByAttendanceDateOrderByEmployee_FirstNameAsc(LocalDate date);

    @Query("""
        SELECT COUNT(a) FROM Attendance a
        WHERE a.employee.id = :empId
          AND a.attendanceDate BETWEEN :from AND :to
          AND a.status IN ('PRESENT', 'HALF_DAY')
        """)
    long countDaysWorked(@Param("empId") Long empId,
                          @Param("from")  LocalDate from,
                          @Param("to")    LocalDate to);

    @Query("""
        SELECT a.status, COUNT(a)
        FROM Attendance a
        WHERE a.employee.id = :empId
          AND a.attendanceDate BETWEEN :from AND :to
        GROUP BY a.status
        """)
    List<Object[]> countByStatusForPeriod(@Param("empId") Long empId,
                                           @Param("from")  LocalDate from,
                                           @Param("to")    LocalDate to);

    @Query("""
        SELECT a.status, COUNT(a)
        FROM Attendance a
        WHERE a.attendanceDate = :today
        GROUP BY a.status
        """)
    List<Object[]> getTodaySummary(@Param("today") LocalDate today);

    @Query("""
        SELECT a.attendanceDate, a.location, COUNT(a)
        FROM Attendance a
        WHERE a.attendanceDate BETWEEN :from AND :to
        GROUP BY a.attendanceDate, a.location
        ORDER BY a.attendanceDate ASC
        """)
    List<Object[]> getWeeklyByLocation(@Param("from") LocalDate from,
                                        @Param("to")   LocalDate to);

    @Query("""
        SELECT AVG(a.workHours)
        FROM Attendance a
        WHERE a.employee.id = :empId
          AND a.attendanceDate BETWEEN :from AND :to
          AND a.workHours IS NOT NULL
        """)
    BigDecimal avgWorkHours(@Param("empId") Long empId,
                             @Param("from")  LocalDate from,
                             @Param("to")    LocalDate to);
}
