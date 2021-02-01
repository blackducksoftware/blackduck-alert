/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.processor.api.extract;

import java.util.EnumMap;
import java.util.List;

import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.processor.api.detail.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.filter.model.ProcessableNotificationWrapper;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@Component
public final class ProviderMessageExtractionDelegator {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final EnumMap<NotificationType, ProviderMessageExtractor> notificationTypeToExtractor;

    @Autowired
    public ProviderMessageExtractionDelegator(List<ProviderMessageExtractor> providerMessageExtractors) {
        this.notificationTypeToExtractor = initializeExtractorMap(providerMessageExtractors);
    }

    public ProviderMessageHolder extract(ProcessableNotificationWrapper filteredNotification) {
        String notificationTypeString = filteredNotification.extractNotificationType();
        NotificationType filteredNotificationType = EnumUtils.getEnum(NotificationType.class, notificationTypeString);
        if (null == filteredNotificationType) {
            logger.warn("Notification did not match any existing notification type: {}", notificationTypeString);
            return ProviderMessageHolder.empty();
        }

        ProviderMessageExtractor providerMessageExtractor = notificationTypeToExtractor.get(filteredNotificationType);
        if (null == providerMessageExtractor) {
            logger.warn("No matching extractor for notification type: {}", notificationTypeString);
            return ProviderMessageHolder.empty();
        }

        return providerMessageExtractor.extract(filteredNotification);
    }

    private EnumMap<NotificationType, ProviderMessageExtractor> initializeExtractorMap(List<ProviderMessageExtractor> providerMessageExtractors) {
        return providerMessageExtractors
                   .stream()
                   .collect(DataStructureUtils.toEnumMap(ProviderMessageExtractor::getNotificationType, NotificationType.class));
    }

}
