/*
 * Copyright ⓒ 2017 by Coradec GmbH.
 *
 * This file is part of the Coradeck.
 *
 * Coradeck is free software: you can redistribute it under the the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License,
 * or any later version.
 *
 * Coradeck is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR ANY PARTICULAR PURPOSE.  See the
 * GNU General Public License for further details.
 *
 * The GNU General Public License is available from <http://www.gnu.org/licenses/>.
 *
 * @license GPL-3.0+ <http://spdx.org/licenses/GPL-3.0+>
 * @author Dominik Wezel <dom@coradec.com>
 *
 */

package com.coradec.coracore.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * ​​Static library of Date utilities.
 */
@SuppressWarnings("UseOfObsoleteDateTimeApi")
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
