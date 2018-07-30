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
package com.blackducksoftware.integration.alert.provider.blackduck.accumulator;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.event.AlertEvent;
import com.blackducksoftware.integration.alert.common.model.NotificationModel;
import com.blackducksoftware.integration.alert.common.model.NotificationModels;
import com.blackducksoftware.integration.alert.workflow.NotificationManager;

@Transactional
public class BlackDuckAccumulatorWriter implements ItemWriter<AlertEvent> {
    private final static Logger logger = LoggerFactory.getLogger(BlackDuckAccumulatorWriter.class);
    private final NotificationManager notificationManager;
    private final ContentConverter contentConverter;

    public BlackDuckAccumulatorWriter(final NotificationManager notificationManager, final ContentConverter contentConverter) {
        this.notificationManager = notificationManager;
        this.contentConverter = contentConverter;
    }

    @Override
    public void write(final List<? extends AlertEvent> itemList) throws Exception {
        try {
            if (itemList != null && !itemList.isEmpty()) {
                logger.info("Writing {} notifications", itemList.size());
                for (final AlertEvent item : itemList) {
                    final Optional<NotificationModels> optionalModel = Optional.ofNullable(contentConverter.getJsonContent(item.getContent(), NotificationModels.class));
                    if (optionalModel.isPresent()) {
                        final NotificationModels notificationModels = optionalModel.get();
                        final List<NotificationModel> notificationList = notificationModels.getNotificationModelList();
                        notificationList.forEach(notificationManager::saveNotification);
                    }
                }
            } else {
                logger.info("No notifications to write");
            }
        } catch (final Exception ex) {
            logger.error("Error occurred writing notification data", ex);
        }
    }
}
