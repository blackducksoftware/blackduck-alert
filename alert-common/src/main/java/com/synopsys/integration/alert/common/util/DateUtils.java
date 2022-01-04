/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

import com.synopsys.integration.rest.RestConstants;

public final class DateUtils {
    public static final String DOCKER_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";
    public static final String AUDIT_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
    public static final String UTC_DATE_FORMAT_TO_MINUTE = "yyyy-MM-dd HH:mm '(UTC)'";

    public static OffsetDateTime createCurrentDateTimestamp() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    public static String createCurrentDateString(String format) {
        OffsetDateTime currentDate = DateUtils.createCurrentDateTimestamp();
        return formatDate(currentDate, format);
    }

    public static String formatDate(OffsetDateTime date, String format) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        return dtf.format(date);
    }

    public static String formatDateAsJsonString(OffsetDateTime dateTime) {
        return formatDate(dateTime, RestConstants.JSON_DATE_FORMAT);
    }

    public static OffsetDateTime fromDateUTC(Date date) {
        return OffsetDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
    }

    public static OffsetDateTime parseDate(String dateTime, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            Date parsedDate = sdf.parse(dateTime);
            return fromDateUTC(parsedDate);
        } catch (DateTimeParseException e) {
            throw new ParseException(e.getParsedString(), e.getErrorIndex());
        }
    }

    public static OffsetDateTime parseDateFromJsonString(String dateTime) throws ParseException {
        return parseDate(dateTime, RestConstants.JSON_DATE_FORMAT);
    }

    private DateUtils() {
    }

}
