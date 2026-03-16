package com.uniquehire.ems.repository;

import com.uniquehire.ems.entity.QrToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QrTokenRepository extends JpaRepository<QrToken, Long> {

    Optional<QrToken> findByTokenHash(String tokenHash);

    boolean existsByTokenHash(String tokenHash);

    @Query("""
        SELECT qt FROM QrToken qt
        WHERE qt.isUsed = false AND qt.expiresAt < :now
        """)
    List<QrToken> findExpiredUnused(@Param("now") ZonedDateTime now);

    @Modifying
    @Transactional
    @Query("DELETE FROM QrToken qt WHERE qt.expiresAt < :cutoff")
    int deleteExpiredBefore(@Param("cutoff") ZonedDateTime cutoff);
}
