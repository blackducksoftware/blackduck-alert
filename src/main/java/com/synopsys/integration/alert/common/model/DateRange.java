/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.common.model;

import java.text.ParseException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import com.synopsys.integration.rest.RestConstants;

public class DateRange {
    private final Date start;
    private final Date end;

    public static final DateRange of(final Date start, final Date end) {
        return new DateRange(start, end);
    }

    public static final DateRange of(final String start, final String end) throws ParseException {
        return new DateRange(RestConstants.parseDateString(start), RestConstants.parseDateString(end));
    }

    public static final Date createCurrentDateTimestamp() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        return Date.from(zonedDateTime.toInstant());
    }

    private DateRange(final Date start, final Date end) {
        this.start = start;
        this.end = end;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }
}
