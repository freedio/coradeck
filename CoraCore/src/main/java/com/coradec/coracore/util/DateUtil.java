package com.coradec.coracore.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * ​​Static library of Date utilities.
 */
public final class DateUtil {

    private DateUtil() {
    }

    /**
     * Checks if the specified date is today.
     *
     * @param date the date to check.
     * @return {@code true} if the specified date refers to the current day.
     */
    public static boolean isToday(LocalDate date) {
        return LocalDate.now().isEqual(date);
    }

    /**
     * Checks if the specified timestamp is today.
     *
     * @param timestamp the timestamp to check.
     * @return {@code true} if the date part of the time stamp refers to the current day.
     */
    public static boolean isToday(LocalDateTime timestamp) {
        return isToday(timestamp.toLocalDate());
    }

    /**
     * Checks if the specified time is today.
     *
     * @param time the time to check.
     * @return always {@code true}
     */
    public static boolean isToday(LocalTime time) {
        return true;
    }

    /**
     * Checks if the specified timestamp is today.
     *
     * @param date the timestamp to check.
     * @return {@code true} if the day part of the timestamp is today as per the system clock.
     */
    public static boolean isToday(Date date) {
        return date.getTime() / (24 * 3600000L) == System.currentTimeMillis() / (24 * 3600000L);
    }

}
