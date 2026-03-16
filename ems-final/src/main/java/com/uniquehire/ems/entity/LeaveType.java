package com.uniquehire.ems.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.ZonedDateTime;

@Entity
@Table(name = "leave_types")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "annual_quota", nullable = false)
    private Integer annualQuota = 0;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = true;

    @Column(name = "carry_forward", nullable = false)
    private Boolean carryForward = false;

    @Column(length = 300)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;
}
