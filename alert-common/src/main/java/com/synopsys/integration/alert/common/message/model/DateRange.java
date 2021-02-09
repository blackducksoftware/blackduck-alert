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
package com.synopsys.integration.alert.common.message.model;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.rest.RestConstants;

public class DateRange extends AlertSerializableModel {
    private final OffsetDateTime start;
    private final OffsetDateTime end;

    public static final DateRange of(OffsetDateTime start, OffsetDateTime end) {
        return new DateRange(start, end);
    }

    public static final DateRange of(String start, String end) throws ParseException {
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
