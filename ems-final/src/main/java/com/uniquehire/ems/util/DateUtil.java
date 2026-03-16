package com.uniquehire.ems.util;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
public class DateUtil {

    /**
     * Count working days (Mon–Fri) between two dates, inclusive.
     */
    public int countWorkingDays(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) return 0;
        int count = 0;
        LocalDate d = from;
        while (!d.isAfter(to)) {
            DayOfWeek dow = d.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) count++;
            d = d.plusDays(1);
        }
        return count;
    }

    /**
     * Returns first day of the given month.
     */
    public LocalDate firstDayOfMonth(LocalDate date) {
        return date.withDayOfMonth(1);
    }

    /**
     * Returns last day of the given month.
     */
    public LocalDate lastDayOfMonth(LocalDate date) {
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    /**
     * Returns true if the given date is a working day (Mon–Fri).
     */
    public boolean isWorkingDay(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }

    /**
     * Parse a month string like "2026-03" into the first day of that month.
     */
    public LocalDate parseMonth(String monthStr) {
        return LocalDate.parse(monthStr + "-01");
    }
}
