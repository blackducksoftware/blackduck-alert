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
package com.blackducksoftware.integration.alert.database.purge;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import com.blackducksoftware.integration.alert.NotificationManager;
import com.blackducksoftware.integration.alert.model.NotificationModel;

@Transactional
public class PurgeWriter implements ItemWriter<List<NotificationModel>> {
    private final static Logger logger = LoggerFactory.getLogger(PurgeWriter.class);
    private final NotificationManager notificationManager;

    public PurgeWriter(final NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    @Override
    public void write(final List<? extends List<NotificationModel>> items) throws Exception {
        try {
            items.forEach(entityList -> {
                if (entityList != null && !entityList.isEmpty()) {
                    logger.info("Purging {} notifications.", entityList.size());
                    notificationManager.deleteNotificationList(entityList);
                }
            });
        } catch (final Exception ex) {
            logger.error("Error purging notifications", ex);
        }
    }
}
