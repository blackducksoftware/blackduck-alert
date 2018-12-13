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
package com.synopsys.integration.alert.web.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.config.TypeConverter;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.entity.NotificationContent;

@Component
public class NotificationContentConverter extends TypeConverter {
    private final Logger logger = LoggerFactory.getLogger(NotificationContentConverter.class);
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    @Autowired
    public NotificationContentConverter(final ContentConverter contentConverter) {
        super(contentConverter);
    }

    @Override
    public Config getConfigFromJson(final String json) {
        return getContentConverter().getJsonContent(json, NotificationConfig.class);
    }

    @Override
    public DatabaseEntity populateEntityFromConfig(final Config restModel) {
        final NotificationConfig notificationConfig = (NotificationConfig) restModel;
        final Date createdAt = parseDateString(notificationConfig.getCreatedAt());
        final Date providerCreationTime = parseDateString(notificationConfig.getProviderCreationTime());
        final NotificationContent notificationEntity = new NotificationContent(createdAt, notificationConfig.getProvider(), providerCreationTime, notificationConfig.getNotificationType(), notificationConfig.getContent());
        addIdToEntityPK(notificationConfig.getId(), notificationEntity);
        return notificationEntity;
    }

    @Override
    public Config populateConfigFromEntity(final DatabaseEntity entity) {
        final NotificationContent notificationEntity = (NotificationContent) entity;
        final String id = getContentConverter().getStringValue(notificationEntity.getId());
        final String createdAt = getContentConverter().getStringValue(notificationEntity.getCreatedAt());
        final String providerCreationTime = getContentConverter().getStringValue(notificationEntity.getProviderCreationTime());
        final NotificationConfig notificationConfig = new NotificationConfig(id, createdAt, notificationEntity.getProvider(), providerCreationTime, notificationEntity.getNotificationType(),
            notificationEntity.getContent());
        return notificationConfig;
    }

    public Date parseDateString(final String dateString) {
        final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (final ParseException e) {
            logger.error(e.toString());
        }
        return date;
    }

}
