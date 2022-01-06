/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.message.model;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.rest.RestConstants;

public class DateRange extends AlertSerializableModel {
    private final OffsetDateTime start;
    private final OffsetDateTime end;

    public static DateRange of(OffsetDateTime start, OffsetDateTime end) {
        return new DateRange(start, end);
    }

    public static DateRange of(String start, String end) throws ParseException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(RestConstants.JSON_DATE_FORMAT);
        try {
            OffsetDateTime startDateTime = OffsetDateTime.parse(start, dtf);
            OffsetDateTime endDateTime = OffsetDateTime.parse(end, dtf);
            return new DateRange(startDateTime, endDateTime);
        } catch (DateTimeParseException e) {
            throw new ParseException(e.getParsedString(), e.getErrorIndex());
        }
    }

    private DateRange(OffsetDateTime start, OffsetDateTime end) {
        this.start = start;
        this.end = end;
    }

    public OffsetDateTime getStart() {
        return start;
    }

    public OffsetDateTime getEnd() {
        return end;
    }

}
