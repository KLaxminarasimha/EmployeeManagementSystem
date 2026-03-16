package com.uniquehire.ems.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.ZonedDateTime;

@Entity
@Table(name = "qr_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QrToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @CreationTimestamp
    @Column(name = "generated_at", updatable = false)
    private ZonedDateTime generatedAt;

    @Column(name = "expires_at", nullable = false)
    private ZonedDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_by")
    private Employee usedBy;

    @Column(name = "used_at")
    private ZonedDateTime usedAt;

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;
}
