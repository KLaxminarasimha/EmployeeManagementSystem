package com.uniquehire.ems.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "attendance",
    uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "attendance_date"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "check_in")
    private ZonedDateTime checkIn;

    @Column(name = "check_out")
    private ZonedDateTime checkOut;

    @Column(name = "work_hours", precision = 4, scale = 2)
    private BigDecimal workHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status = AttendanceStatus.PRESENT;

    @Enumerated(EnumType.STRING)
    @Column(name = "mark_method", nullable = false, length = 20)
    private MarkMethod markMethod = MarkMethod.QR;

    @Column(name = "wifi_ssid", length = 100)
    private String wifiSsid;

    @Column(name = "qr_token_used", length = 100)
    private String qrTokenUsed;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Location location = Location.OFFICE;

    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marked_by")
    private Employee markedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    public enum AttendanceStatus { PRESENT, ABSENT, HALF_DAY, HOLIDAY, WEEKEND }
    public enum MarkMethod        { QR, WIFI_AUTO, MANUAL, WFH_AUTO }
    public enum Location          { OFFICE, WFH, REMOTE }
}
