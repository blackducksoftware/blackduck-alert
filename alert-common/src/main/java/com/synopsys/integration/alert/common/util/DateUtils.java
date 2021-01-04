/**
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

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

    private DateUtils() {
    }

}
