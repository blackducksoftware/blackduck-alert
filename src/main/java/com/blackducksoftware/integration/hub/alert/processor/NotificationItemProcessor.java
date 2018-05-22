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
package com.blackducksoftware.integration.hub.alert.processor;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.integration.hub.alert.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResult;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;

public class NotificationItemProcessor {
    private final List<NotificationTypeProcessor<?>> processorList;

    public NotificationItemProcessor(final List<NotificationTypeProcessor<?>> processorList) {
        this.processorList = processorList;
    }

    public DBStoreEvent process(final NotificationDetailResults notificationData) {
        final List<NotificationDetailResult> resultList = notificationData.getResults();
        final HubBucket bucket = notificationData.getHubBucket();
        final List<NotificationModel> notificationList = new ArrayList<>(resultList.size());
        resultList.forEach(notificationViewResult -> {
            notificationList.addAll(createModels(notificationViewResult, bucket));
        });

        return new DBStoreEvent(notificationList);
    }

    private List<NotificationModel> createModels(final NotificationDetailResult notificationDetailResult, final HubBucket bucket) {
        final List<NotificationModel> modelList = new ArrayList<>(50);

        processorList.forEach(processor -> {
            if (processor.isApplicable(notificationDetailResult)) {
                processor.process(notificationDetailResult, bucket);
            }
        });

        processorList.forEach(processor -> {
            modelList.addAll(processor.getModels(bucket));
        });

        return modelList;
    }
}
