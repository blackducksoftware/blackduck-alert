/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.digest;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.blackducksoftware.integration.hub.alert.NotificationManager;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.PhoneHomeService;
import com.blackducksoftware.integration.hub.service.model.PhoneHomeResponse;
import com.blackducksoftware.integration.phonehome.PhoneHomeRequestBody;
import com.blackducksoftware.integration.phonehome.PhoneHomeRequestBodyBuilder;
import com.blackducksoftware.integration.phonehome.enums.ThirdPartyName;

public class DailyItemReader extends DigestItemReader {
    private final static Logger logger = LoggerFactory.getLogger(DailyItemReader.class);
    private final GlobalProperties globalProperties;

    public DailyItemReader(final NotificationManager notificationManager, final GlobalProperties globalProperties) {
        super(DailyItemReader.class.getName(), notificationManager);
        this.globalProperties = globalProperties;
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

    @Override
    public List<NotificationModel> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        List<NotificationModel> notificationList;
        PhoneHomeResponse phoneHomeResponse = null;
        try {
            notificationList = super.read();
            phoneHomeResponse = phoneHome();
        } finally {
            if (phoneHomeResponse != null) {
                phoneHomeResponse.endPhoneHome();
            }
        }

        return notificationList;
    }

    private PhoneHomeResponse phoneHome() {
        final String productVersion = globalProperties.getProductVersion();
        if (!GlobalProperties.PRODUCT_VERSION_UNKNOWN.equals(productVersion)) {
            final HubServicesFactory hubServicesFactory = globalProperties.createHubServicesFactoryAndLogErrors(logger);
            final PhoneHomeService phoneHomeService = hubServicesFactory.createPhoneHomeService();
            final PhoneHomeRequestBodyBuilder phoneHomeRequestBodyBuilder = phoneHomeService.createInitialPhoneHomeRequestBodyBuilder(ThirdPartyName.ALERT, productVersion, productVersion);
            final PhoneHomeRequestBody phoneHomeRequestBody = phoneHomeRequestBodyBuilder.build();
            return phoneHomeService.startPhoneHome(phoneHomeRequestBody);
        } else {
            return null;
        }
    }
}
