package com.uniquehire.ems.repository;

import com.uniquehire.ems.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployee_IdOrderByCreatedAtDesc(Long employeeId);

    List<LeaveRequest> findByStatusOrderByCreatedAtAsc(LeaveRequest.LeaveStatus status);

    long countByStatus(LeaveRequest.LeaveStatus status);

    @Query("""
        SELECT lr FROM LeaveRequest lr
        WHERE lr.status = 'PENDING'
          AND lr.employee.manager.id = :managerId
        ORDER BY lr.createdAt ASC
        """)
    List<LeaveRequest> findPendingForManager(@Param("managerId") Long managerId);

    @Query("""
        SELECT COUNT(lr) FROM LeaveRequest lr
        WHERE lr.employee.id = :empId
          AND lr.status IN ('PENDING','APPROVED')
          AND lr.fromDate <= :to
          AND lr.toDate   >= :from
        """)
    long countOverlapping(@Param("empId") Long empId,
                           @Param("from")  LocalDate from,
                           @Param("to")    LocalDate to);

    @Query("""
        SELECT lr.leaveType.name, SUM(lr.numDays)
        FROM LeaveRequest lr
        WHERE lr.employee.id = :empId
          AND lr.status = 'APPROVED'
          AND FUNCTION('YEAR', lr.fromDate) = :year
        GROUP BY lr.leaveType.name
        """)
    List<Object[]> getLeaveUsageByType(@Param("empId") Long empId,
                                        @Param("year")  int year);

    @Query("""
        SELECT lr FROM LeaveRequest lr
        WHERE lr.status = 'APPROVED'
          AND :today BETWEEN lr.fromDate AND lr.toDate
        """)
    List<LeaveRequest> findCurrentlyOnLeave(@Param("today") LocalDate today);
}
