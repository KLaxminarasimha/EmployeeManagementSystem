package com.uniquehire.ems.service.interfaces;

import com.uniquehire.ems.dto.*;

import java.math.BigDecimal;
import java.util.List;

public interface IPerformanceService {

    PerformanceReviewResponse       createReview(PerformanceReviewRequest request);
    PerformanceReviewResponse       updateReview(Long reviewId, PerformanceReviewRequest request);
    PerformanceReviewResponse       submitReview(Long reviewId);
    PerformanceReviewResponse       acknowledgeReview(Long reviewId);
    PerformanceReviewResponse       getReviewById(Long reviewId);
    List<PerformanceReviewResponse> getReviewsByPeriod(String period);
    List<PerformanceReviewResponse> getEmployeeHistory(Long employeeId);
    List<PerformanceReviewResponse> getTopPerformers(String period, int limit);
    List<PerformanceTrendDto>       getPerformanceTrend();
    String                          deriveRating(BigDecimal score);
}
