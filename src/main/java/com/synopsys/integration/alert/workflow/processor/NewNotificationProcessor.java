/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.workflow.processor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.provider.notification.ProviderDistributionFilter;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.workflow.cache.NotificationDeserializationCache;

@Component
public class NewNotificationProcessor {
    private ConfigurationAccessor configurationAccessor;
    private Map<String, Provider> providerKeyToProvider;

    @Autowired
    public NewNotificationProcessor(ConfigurationAccessor configurationAccessor, List<Provider> providers) {
        this.configurationAccessor = configurationAccessor;
        this.providerKeyToProvider = initializeProviderMap(providers);
    }

    public List<DistributionEvent> processNotifications(FrequencyType frequency, List<AlertNotificationWrapper> notifications) {
        if (notifications.isEmpty()) {
            return List.of();
        }

        List<ConfigurationJobModel> jobsForFrequency = getJobsForFrequency(frequency);
        if (jobsForFrequency.isEmpty()) {
            return List.of();
        }

        List<DistributionEvent> distributionEvents = new LinkedList<>();
        for (ConfigurationJobModel job : jobsForFrequency) {
            Provider provider = providerKeyToProvider.get(job.getProviderName());
            ProviderDistributionFilter distributionFilter = provider.createDistributionFilter();
            List<AlertNotificationWrapper> filteredNotifications = filterNotificationsByJobFields(job, distributionFilter, notifications);
            if (!filteredNotifications.isEmpty()) {
                List<DistributionEvent> events = createDistributionEventsForNotifications(job, distributionFilter.getCache(), filteredNotifications);
                distributionEvents.addAll(events);
            }
        }
        return distributionEvents;
    }

    private List<ConfigurationJobModel> getJobsForFrequency(FrequencyType frequency) {
        return configurationAccessor.getAllJobs()
                   .stream()
                   .filter(job -> frequency == job.getFrequencyType())
                   .collect(Collectors.toList());
    }

    private List<AlertNotificationWrapper> filterNotificationsByJobFields(ConfigurationJobModel job, ProviderDistributionFilter distributionFilter, List<AlertNotificationWrapper> notifications) {
        List<AlertNotificationWrapper> filteredNotifications = new LinkedList<>();
        for (AlertNotificationWrapper notification : notifications) {
            if (distributionFilter.doesNotificationApplyToConfiguration(notification, job)) {
                filteredNotifications.add(notification);
            }
        }
        return filteredNotifications;
    }

    private List<DistributionEvent> createDistributionEventsForNotifications(ConfigurationJobModel job, NotificationDeserializationCache cache, List<AlertNotificationWrapper> notifications) {
        // FIXME implement
        return List.of();
    }

    private Map<String, Provider> initializeProviderMap(List<Provider> providers) {
        return providers
                   .stream()
                   .collect(Collectors.toMap(provider -> provider.getKey().getUniversalKey(), Function.identity()));
    }

}
