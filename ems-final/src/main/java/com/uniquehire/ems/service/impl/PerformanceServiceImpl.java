package com.uniquehire.ems.service.impl;

import com.uniquehire.ems.dto.*;
import com.uniquehire.ems.entity.*;
import com.uniquehire.ems.exception.*;
import com.uniquehire.ems.repository.*;
import com.uniquehire.ems.service.interfaces.IPerformanceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PerformanceServiceImpl implements IPerformanceService {

    private static final Logger log = LoggerFactory.getLogger(PerformanceServiceImpl.class);

    private final PerformanceReviewRepository perfRepo;
    private final EmployeeRepository          employeeRepo;

    // ── CREATE ───────────────────────────────────────────────

    @Override
    @Transactional
    public PerformanceReviewResponse createReview(PerformanceReviewRequest req) {
        Employee employee = findEmpOrThrow(req.getEmployeeId());
        Employee reviewer = findEmpOrThrow(req.getReviewerId());

        if (perfRepo.findByEmployee_IdAndReviewPeriod(
                req.getEmployeeId(), req.getReviewPeriod()).isPresent())
            throw new BusinessException(
                "A review already exists for " + employee.getFullName()
                + " for period " + req.getReviewPeriod());

        PerformanceReview review = perfRepo.save(PerformanceReview.builder()
            .employee(employee).reviewer(reviewer)
            .reviewPeriod(req.getReviewPeriod()).reviewDate(req.getReviewDate())
            .score(req.getScore().setScale(1, RoundingMode.HALF_UP))
            .rating(deriveRating(req.getScore()))
            .strengths(req.getStrengths()).improvements(req.getImprovements())
            .goalsNext(req.getGoalsNext())
            .status(PerformanceReview.ReviewStatus.DRAFT).build());

        log.info("Review created → id={} emp={} score={}", review.getId(),
            employee.getFullName(), review.getScore());
        return toResponse(review);
    }

    // ── UPDATE (DRAFT only) ──────────────────────────────────

    @Override
    @Transactional
    public PerformanceReviewResponse updateReview(Long reviewId, PerformanceReviewRequest req) {
        PerformanceReview review = findReviewOrThrow(reviewId);
        if (review.getStatus() != PerformanceReview.ReviewStatus.DRAFT)
            throw new BusinessException("Only DRAFT reviews can be updated. Status: " + review.getStatus());

        review.setScore(req.getScore().setScale(1, RoundingMode.HALF_UP));
        review.setRating(deriveRating(req.getScore()));
        review.setStrengths(req.getStrengths());
        review.setImprovements(req.getImprovements());
        review.setGoalsNext(req.getGoalsNext());
        review.setReviewDate(req.getReviewDate());
        return toResponse(perfRepo.save(review));
    }

    // ── SUBMIT (DRAFT → SUBMITTED) ───────────────────────────

    @Override
    @Transactional
    public PerformanceReviewResponse submitReview(Long reviewId) {
        PerformanceReview review = findReviewOrThrow(reviewId);
        if (review.getStatus() != PerformanceReview.ReviewStatus.DRAFT)
            throw new BusinessException("Only DRAFT reviews can be submitted.");
        if (review.getStrengths() == null || review.getStrengths().isBlank())
            throw new BusinessException("Strengths field is required before submitting.");
        if (review.getImprovements() == null || review.getImprovements().isBlank())
            throw new BusinessException("Improvements field is required before submitting.");
        review.setStatus(PerformanceReview.ReviewStatus.SUBMITTED);
        return toResponse(perfRepo.save(review));
    }

    // ── ACKNOWLEDGE (SUBMITTED → ACKNOWLEDGED) ───────────────

    @Override
    @Transactional
    public PerformanceReviewResponse acknowledgeReview(Long reviewId) {
        PerformanceReview review = findReviewOrThrow(reviewId);
        if (review.getStatus() != PerformanceReview.ReviewStatus.SUBMITTED)
            throw new BusinessException("Only SUBMITTED reviews can be acknowledged.");
        review.setStatus(PerformanceReview.ReviewStatus.ACKNOWLEDGED);
        return toResponse(perfRepo.save(review));
    }

    // ── GETTERS ──────────────────────────────────────────────

    @Override
    public PerformanceReviewResponse getReviewById(Long reviewId) {
        return toResponse(findReviewOrThrow(reviewId));
    }

    @Override
    public List<PerformanceReviewResponse> getReviewsByPeriod(String period) {
        return perfRepo.findByReviewPeriodOrderByScoreDesc(period)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<PerformanceReviewResponse> getEmployeeHistory(Long empId) {
        findEmpOrThrow(empId);
        return perfRepo.findByEmployee_IdOrderByReviewDateDesc(empId)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<PerformanceReviewResponse> getTopPerformers(String period, int limit) {
        return perfRepo.getTopPerformers(period, PageRequest.of(0, limit))
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<PerformanceTrendDto> getPerformanceTrend() {
        return perfRepo.getAverageScoreByPeriod().stream()
            .map(r -> PerformanceTrendDto.builder()
                .period((String) r[0])
                .avgScore(((BigDecimal) r[1]).setScale(1, RoundingMode.HALF_UP)).build())
            .collect(Collectors.toList());
    }

    // ── RATING DERIVATION ────────────────────────────────────

    @Override
    public String deriveRating(BigDecimal score) {
        if (score == null) return "UNRATED";
        int s = score.intValue();
        if (s >= 90) return "EXCELLENT";
        if (s >= 75) return "GOOD";
        if (s >= 60) return "AVERAGE";
        return "NEEDS_IMPROVEMENT";
    }

    // ── PRIVATE HELPERS ──────────────────────────────────────

    private PerformanceReview findReviewOrThrow(Long id) {
        return perfRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("PerformanceReview", id));
    }

    private Employee findEmpOrThrow(Long id) {
        return employeeRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    }

    // ── MAPPER ───────────────────────────────────────────────

    public PerformanceReviewResponse toResponse(PerformanceReview r) {
        return PerformanceReviewResponse.builder()
            .id(r.getId()).employeeId(r.getEmployee().getId())
            .employeeName(r.getEmployee().getFullName())
            .reviewerName(r.getReviewer() != null ? r.getReviewer().getFullName() : null)
            .reviewPeriod(r.getReviewPeriod()).reviewDate(r.getReviewDate())
            .score(r.getScore()).rating(r.getRating())
            .strengths(r.getStrengths()).improvements(r.getImprovements())
            .goalsNext(r.getGoalsNext()).status(r.getStatus().name())
            .createdAt(r.getCreatedAt()).build();
    }
}
