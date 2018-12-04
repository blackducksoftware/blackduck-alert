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

import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.entity.NotificationContent;

@Component
public class NotificationContentConverter extends TypeConverter {

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
        final Date createdAt = getContentConverter().getValue(notificationConfig.getCreatedAt(), Date.class);
        final Date providerCreationTime = getContentConverter().getValue(notificationConfig.getProviderCreationTime(), Date.class);
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

}
