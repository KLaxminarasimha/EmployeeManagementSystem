package com.uniquehire.ems.repository;

import com.uniquehire.ems.entity.PerformanceReview;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {

    List<PerformanceReview> findByEmployee_IdOrderByReviewDateDesc(Long employeeId);

    Optional<PerformanceReview> findByEmployee_IdAndReviewPeriod(Long employeeId, String reviewPeriod);

    List<PerformanceReview> findByReviewPeriodOrderByScoreDesc(String reviewPeriod);

    @Query("""
        SELECT p.reviewPeriod, AVG(p.score)
        FROM PerformanceReview p
        WHERE p.status = 'SUBMITTED'
        GROUP BY p.reviewPeriod
        ORDER BY p.reviewPeriod ASC
        """)
    List<Object[]> getAverageScoreByPeriod();

    @Query("""
        SELECT p FROM PerformanceReview p
        WHERE p.reviewPeriod = :period AND p.status = 'SUBMITTED'
        ORDER BY p.score DESC
        """)
    List<PerformanceReview> getTopPerformers(@Param("period") String period, Pageable pageable);
}
