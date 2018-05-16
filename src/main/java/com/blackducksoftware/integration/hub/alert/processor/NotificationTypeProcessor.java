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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.notification.NotificationViewResult;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;

public abstract class NotificationTypeProcessor<M extends NotificationProcessingModel> {
    private final Map<String, M> modelMap = new LinkedHashMap<>(500);
    private final Collection<NotificationProcessingRule<M>> processingRules;

    public NotificationTypeProcessor(final Collection<NotificationProcessingRule<M>> processingRules) {
        this.processingRules = processingRules;
    }

    public Collection<NotificationProcessingRule<M>> getProcessingRules() {
        return processingRules;
    }

    public boolean isApplicable(final NotificationViewResult notificationViewResult) {
        final boolean isApplicable = processingRules.parallelStream().anyMatch(rule -> {
            return rule.isApplicable(notificationViewResult);
        });
        return isApplicable;
    }

    public void process(final NotificationViewResult notificationViewResult, final HubBucket bucket) {
        processingRules.forEach(rule -> {
            if (rule.isApplicable(notificationViewResult)) {
                rule.apply(getModelMap(), notificationViewResult, bucket);
            }
        });
    }

    public List<NotificationModel> getModels(final HubBucket bucket) {
        final List<NotificationModel> unsortedModelList = createModelList();
        final List<NotificationModel> modelList = unsortedModelList.stream().sorted((model1, model2) -> {
            return model2.getCreatedAt().compareTo(model1.getCreatedAt());
        }).collect(Collectors.toList());
        return modelList;
    }

    protected Map<String, M> getModelMap() {
        return modelMap;
    }

    protected abstract List<NotificationModel> createModelList();
}
