package com.uniquehire.ems.service.interfaces;

import com.uniquehire.ems.dto.DashboardStatsResponse;

public interface IDashboardService {

    /**
     * Returns all dashboard stat cards in a single DB-aggregated call:
     * employee counts, today's attendance, pending leaves,
     * payroll this month, avg performance score, dept breakdown, payroll trend.
     */
    DashboardStatsResponse getDashboardStats();
}
