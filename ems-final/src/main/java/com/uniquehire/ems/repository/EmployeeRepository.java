package com.uniquehire.ems.repository;

import com.uniquehire.ems.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByEmployeeCode(String employeeCode);
    boolean existsByEmail(String email);

    Page<Employee> findByStatus(Employee.EmployeeStatus status, Pageable pageable);
    List<Employee> findByStatus(Employee.EmployeeStatus status);
    long countByStatus(Employee.EmployeeStatus status);

    Page<Employee> findByDepartment_Name(String deptName, Pageable pageable);
    Page<Employee> findByDepartment_NameAndStatus(String deptName, Employee.EmployeeStatus status, Pageable pageable);

    List<Employee> findByWorkModeAndStatus(Employee.WorkMode workMode, Employee.EmployeeStatus status);
    List<Employee> findByManager_Id(Long managerId);

    @Query("""
        SELECT e FROM Employee e
        WHERE e.status = 'ACTIVE'
          AND (
            LOWER(CONCAT(e.firstName,' ',e.lastName)) LIKE LOWER(CONCAT('%',:q,'%'))
            OR LOWER(e.email)       LIKE LOWER(CONCAT('%',:q,'%'))
            OR LOWER(e.designation) LIKE LOWER(CONCAT('%',:q,'%'))
          )
        ORDER BY e.firstName ASC
        """)
    Page<Employee> searchActive(@Param("q") String query, Pageable pageable);

    @Query("""
        SELECT e.department.name, COUNT(e)
        FROM Employee e
        WHERE e.status = 'ACTIVE' AND e.department IS NOT NULL
        GROUP BY e.department.name
        ORDER BY COUNT(e) DESC
        """)
    List<Object[]> countByDepartment();

    @Query("SELECT MAX(e.employeeCode) FROM Employee e WHERE e.employeeCode LIKE 'UH-%'")
    Optional<String> findMaxEmployeeCode();

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.dateOfJoining >= :from AND e.status = 'ACTIVE'")
    long countNewHiresSince(@Param("from") LocalDate from);

    @Query("SELECT e FROM Employee e WHERE e.manager.id = :managerId AND e.status = 'ACTIVE' ORDER BY e.firstName")
    List<Employee> findActiveDirectReports(@Param("managerId") Long managerId);
}
