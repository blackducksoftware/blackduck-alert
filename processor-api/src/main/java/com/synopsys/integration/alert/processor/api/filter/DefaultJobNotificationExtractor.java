/**
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
package com.synopsys.integration.alert.processor.api.filter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobResponseModel;
import com.synopsys.integration.alert.processor.api.filter.model.FilterableNotificationWrapper;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@Component
public class DefaultJobNotificationExtractor implements JobNotificationExtractor {
    private JobAccessor jobAccessor;

    @Autowired
    public DefaultJobNotificationExtractor(JobAccessor jobAccessor) {
        this.jobAccessor = jobAccessor;
    }

    /*
     * Filter Items:
     * Frequency (Passed into processor)
     * Notification Type (From notification)
     * Filter By Project (Projects from notification if applicable)
     *   Project Name
     *   Project Name Pattern
     * Filter by Vulnerability severity (From notification if applicable)
     * Filter by Policy name (From notification if applicable)
     */

    @Override
    public Map<FilteredDistributionJobResponseModel, List<FilterableNotificationWrapper<?>>> mapJobsToNotifications(List<? extends FilterableNotificationWrapper<?>> filterableNotifications, @Nullable FrequencyType frequency) {
        Map<FilteredDistributionJobResponseModel, List<FilterableNotificationWrapper<?>>> groupedFilterableNotifications = new HashMap<>();

        for (FilterableNotificationWrapper filterableNotificationWrapper : filterableNotifications) {
            List<FilteredDistributionJobResponseModel> filteredDistributionJobResponseModels = retrieveMatchingJobs(filterableNotificationWrapper, frequency);
            for (FilteredDistributionJobResponseModel filteredDistributionJobResponseModel : filteredDistributionJobResponseModels) {
                List<FilterableNotificationWrapper<?>> set = groupedFilterableNotifications.computeIfAbsent(filteredDistributionJobResponseModel, ignoredKey -> new LinkedList<>());
                set.add(filterableNotificationWrapper);
            }
        }
        return groupedFilterableNotifications;
    }

    private List<FilteredDistributionJobResponseModel> retrieveMatchingJobs(FilterableNotificationWrapper filterableNotificationWrapper, @Nullable FrequencyType frequencyType) {
        // If a frequency is null, we assume they want all frequency types as a frequency is required.
        List<FrequencyType> frequencyTypes = (frequencyType == null) ? Arrays.stream(FrequencyType.values()).collect(Collectors.toList()) : List.of(frequencyType);

        FilteredDistributionJobRequestModel filteredDistributionJobRequestModel = new FilteredDistributionJobRequestModel(
            frequencyTypes,
            EnumUtils.getEnum(NotificationType.class, filterableNotificationWrapper.extractNotificationType()),
            filterableNotificationWrapper.getProjectName(),
            filterableNotificationWrapper.getVulnerabilitySeverities(),
            filterableNotificationWrapper.getPolicyNames()
        );
        return jobAccessor.getMatchingEnabledJobs(filteredDistributionJobRequestModel);
    }

}
