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
package com.blackducksoftware.integration.hub.alert.provider.hub.accumulator;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.event.AlertEvent;
import com.blackducksoftware.integration.hub.alert.event.AlertEventContentConverter;
import com.blackducksoftware.integration.hub.alert.processor.NotificationItemProcessor;
import com.blackducksoftware.integration.hub.alert.processor.NotificationTypeProcessor;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;

public class AccumulatorProcessor implements ItemProcessor<NotificationDetailResults, AlertEvent> {
    private final Logger logger = LoggerFactory.getLogger(AccumulatorProcessor.class);
    private final GlobalProperties globalProperties;
    private final List<NotificationTypeProcessor> notificationProcessors;
    private final AlertEventContentConverter contentConverter;

    public AccumulatorProcessor(final GlobalProperties globalProperties, final List<NotificationTypeProcessor> notificationProcessors, final AlertEventContentConverter contentConverter) {
        this.globalProperties = globalProperties;
        this.notificationProcessors = notificationProcessors;
        this.contentConverter = contentConverter;
    }

    @Override
    public AlertEvent process(final NotificationDetailResults notificationData) throws Exception {
        if (notificationData != null) {
            try {
                logger.info("Processing accumulated notifications");
                final NotificationItemProcessor notificationItemProcessor = new NotificationItemProcessor(notificationProcessors, contentConverter);
                final AlertEvent storeEvent = notificationItemProcessor.process(globalProperties, notificationData);
                return storeEvent;
            } catch (final Exception ex) {
                logger.error("Error occurred durring processing of accumulated notifications", ex);
            }
        } else {
            logger.info("No notifications to process");
        }
        return null;
    }
}
