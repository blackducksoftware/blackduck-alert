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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.event.AlertEvent;
import com.blackducksoftware.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackducksoftware.integration.alert.workflow.processor.NotificationItemProcessor;
import com.blackducksoftware.integration.alert.workflow.processor.NotificationTypeProcessor;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;

public class BlackDuckAccumulatorProcessor implements ItemProcessor<NotificationDetailResults, AlertEvent> {
    private final Logger logger = LoggerFactory.getLogger(BlackDuckAccumulatorProcessor.class);
    private final BlackDuckProperties blackDuckProperties;
    private final List<NotificationTypeProcessor> notificationProcessors;
    private final ContentConverter contentConverter;

    public BlackDuckAccumulatorProcessor(final BlackDuckProperties blackDuckProperties, final List<NotificationTypeProcessor> notificationProcessors, final ContentConverter contentConverter) {
        this.blackDuckProperties = blackDuckProperties;
        this.notificationProcessors = notificationProcessors;
        this.contentConverter = contentConverter;
    }

    @Override
    public AlertEvent process(final NotificationDetailResults notificationData) throws Exception {
        if (notificationData != null) {
            try {
                logger.info("Processing accumulated notifications");
                final NotificationItemProcessor notificationItemProcessor = new NotificationItemProcessor(notificationProcessors, contentConverter);
                final AlertEvent storeEvent = notificationItemProcessor.process(blackDuckProperties, notificationData);
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
