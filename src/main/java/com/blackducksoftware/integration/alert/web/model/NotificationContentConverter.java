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
package com.blackducksoftware.integration.alert.web.model;

import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.descriptor.DatabaseContentConverter;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.database.entity.NotificationEntity;

@Component
public class NotificationContentConverter extends DatabaseContentConverter {

    @Autowired
    public NotificationContentConverter(final ContentConverter contentConverter) {
        super(contentConverter);
    }

    @Override
    public ConfigRestModel getRestModelFromJson(final String json) {
        return getContentConverter().getJsonContent(json, NotificationRestModel.class);
    }

    @Override
    public DatabaseEntity populateDatabaseEntityFromRestModel(final ConfigRestModel restModel) {
        final NotificationRestModel notificationRestModel = (NotificationRestModel) restModel;
        final Date createdAt = getContentConverter().getValue(notificationRestModel.getCreatedAt(), Date.class);
        final NotificationEntity notificationEntity = new NotificationEntity(notificationRestModel.getEventKey(), createdAt, null, notificationRestModel.getProjectName(), notificationRestModel.getProjectUrl(),
                notificationRestModel.getProjectVersion(), notificationRestModel.getProjectVersionUrl(), null, null, null, null);
        addIdToEntityPK(notificationRestModel.getId(), notificationEntity);
        return notificationEntity;
    }

    @Override
    public ConfigRestModel populateRestModelFromDatabaseEntity(final DatabaseEntity entity) {
        final NotificationEntity notificationEntity = (NotificationEntity) entity;
        final String id = getContentConverter().getStringValue(notificationEntity.getId());
        final String createdAt = getContentConverter().getStringValue(notificationEntity.getCreatedAt());
        final NotificationRestModel notificationRestModel = new NotificationRestModel(id, notificationEntity.getEventKey(), createdAt, null, notificationEntity.getProjectName(), notificationEntity.getProjectVersion(), null,
                notificationEntity.getProjectUrl(), notificationEntity.getProjectVersionUrl());
        return notificationRestModel;
    }

}
