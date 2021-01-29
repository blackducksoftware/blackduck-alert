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
package com.synopsys.integration.alert.processor.api.filter;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.EnumUtils;
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

    /**
     * Jobs are retrieved from the DB depending on the following fields that are passed to this method:
     *
     * Frequency (Passed into processor)
     * Notification Type (From notification)
     * Filter By Project (Projects from notification if applicable)
     * Project Name (Found in Job and based on project)
     * Project Name Pattern (Found in Job and based on Project)
     * Filter by Vulnerability severity (From notification if applicable)
     * Filter by Policy name (From notification if applicable)
     * @param filterableNotifications List of notifications that will be iterated over and applied to jobs that are found
     * @param frequencies             an Additional filter to specify when querying data from the DB
     * @return a {@code Map} where the distribution job is used to map to a list of notifications that were passed in.
     */
    @Override
    public Map<FilteredDistributionJobResponseModel, List<FilterableNotificationWrapper<?>>> mapJobsToNotifications(List<? extends FilterableNotificationWrapper<?>> filterableNotifications, Collection<FrequencyType> frequencies) {
        Map<FilteredDistributionJobResponseModel, List<FilterableNotificationWrapper<?>>> groupedFilterableNotifications = new HashMap<>();

        for (FilterableNotificationWrapper filterableNotificationWrapper : filterableNotifications) {
            List<FilteredDistributionJobResponseModel> filteredDistributionJobResponseModels = retrieveMatchingJobs(filterableNotificationWrapper, frequencies);
            for (FilteredDistributionJobResponseModel filteredDistributionJobResponseModel : filteredDistributionJobResponseModels) {
                List<FilterableNotificationWrapper<?>> set = groupedFilterableNotifications.computeIfAbsent(filteredDistributionJobResponseModel, ignoredKey -> new LinkedList<>());
                set.add(filterableNotificationWrapper);
            }
        }
        return groupedFilterableNotifications;
    }

    private List<FilteredDistributionJobResponseModel> retrieveMatchingJobs(FilterableNotificationWrapper filterableNotificationWrapper, Collection<FrequencyType> frequencyTypes) {
        FilteredDistributionJobRequestModel filteredDistributionJobRequestModel = new FilteredDistributionJobRequestModel(
            frequencyTypes,
            EnumUtils.getEnum(NotificationType.class, filterableNotificationWrapper.extractNotificationType()),
            filterableNotificationWrapper.getProjectNames(),
            filterableNotificationWrapper.getVulnerabilitySeverities(),
            filterableNotificationWrapper.getPolicyNames()
        );
        return jobAccessor.getMatchingEnabledJobs(filteredDistributionJobRequestModel);
    }

}
