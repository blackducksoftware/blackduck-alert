/**
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.digest;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.NotificationRepositoryWrapper;

public class DailyItemReader extends DigestItemReader {

    public DailyItemReader(final NotificationRepositoryWrapper notificationRepository) {
        super(DailyItemReader.class.getName(), notificationRepository);
    }

    @Override
    public DateRange getDateRange() {
        ZonedDateTime currentTime = ZonedDateTime.now();
        currentTime = currentTime.withZoneSameInstant(ZoneOffset.UTC);
        final ZonedDateTime zonedEndDate = currentTime.withHour(23).withMinute(59).withSecond(59).withNano(9999);
        final ZonedDateTime zonedStartDate = currentTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
        final Date startDate = Date.from(zonedStartDate.toInstant());
        final Date endDate = Date.from(zonedEndDate.toInstant());
        return new DateRange(startDate, endDate);
    }
}
