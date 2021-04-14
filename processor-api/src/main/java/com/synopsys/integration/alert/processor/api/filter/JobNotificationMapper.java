/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.filter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingJobAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobResponseModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;

@Component
public class JobNotificationMapper {
    private static final int PAGE_SIZE = 100;
    private static final int INITIAL_PAGE_NUMBER = 0;

    private final ProcessingJobAccessor processingJobAccessor;

    @Autowired
    public JobNotificationMapper(ProcessingJobAccessor processingJobAccessor) {
        this.processingJobAccessor = processingJobAccessor;
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
     * @param detailedContents List of notifications that will be iterated over and applied to jobs that are found
     * @param frequencies      an Additional filter to specify when querying data from the DB
     * @return a {@code StatefulAlertPagedModel} where a page of distributions job is used to map to a list of notifications that were passed in.
     */
    public StatefulAlertPagedModel<FilteredJobNotificationWrapper> mapJobsToNotifications(List<DetailedNotificationContent> detailedContents, List<FrequencyType> frequencies) {
        AlertPagedModel<FilteredJobNotificationWrapper> pageOfJobsToNotification = mapPageOfJobsToNotification(detailedContents, frequencies, INITIAL_PAGE_NUMBER, PAGE_SIZE);
        return new StatefulAlertPagedModel<>(
            pageOfJobsToNotification.getTotalPages(),
            pageOfJobsToNotification.getCurrentPage(),
            pageOfJobsToNotification.getPageSize(),
            pageOfJobsToNotification.getModels(),
            (number, size) -> mapPageOfJobsToNotification(detailedContents, frequencies, number, size)
        );
    }

    private AlertPagedModel<FilteredJobNotificationWrapper> mapPageOfJobsToNotification(List<DetailedNotificationContent> detailedContents, List<FrequencyType> frequencies, int pageNumber, int pageSize) {
        Map<FilteredDistributionJobResponseModel, List<NotificationContentWrapper>> groupedFilterableNotifications = new HashMap<>();

        FilteredDistributionJobRequestModel filteredDistributionJobRequestModel = new FilteredDistributionJobRequestModel(frequencies);
        for (DetailedNotificationContent detailedNotificationContent : detailedContents) {
            detailedNotificationContent.getProjectName().ifPresent(filteredDistributionJobRequestModel::addProjectName);
            filteredDistributionJobRequestModel.addNotificationType(detailedNotificationContent.getNotificationContentWrapper().extractNotificationType());
            filteredDistributionJobRequestModel.addVulnerabilitySeverities(detailedNotificationContent.getVulnerabilitySeverities());
            detailedNotificationContent.getPolicyName().ifPresent(filteredDistributionJobRequestModel::addPolicyName);
        }

        AlertPagedModel<FilteredDistributionJobResponseModel> jobs = processingJobAccessor.getMatchingEnabledJobsByFilteredNotifications(filteredDistributionJobRequestModel, pageNumber, pageSize);
        for (DetailedNotificationContent detailedNotificationContent : detailedContents) {
            for (FilteredDistributionJobResponseModel filteredDistributionJobResponseModel : jobs.getModels()) {
                if (JobNotificationFilterUtils.doesNotificationApplyToJob(filteredDistributionJobResponseModel, detailedNotificationContent)) {
                    List<NotificationContentWrapper> applicableNotifications = groupedFilterableNotifications.computeIfAbsent(filteredDistributionJobResponseModel, ignoredKey -> new LinkedList<>());
                    applicableNotifications.add(detailedNotificationContent.getNotificationContentWrapper());
                }
            }
        }

        List<FilteredJobNotificationWrapper> filterableJobNotifications = new LinkedList<>();
        for (Map.Entry<FilteredDistributionJobResponseModel, List<NotificationContentWrapper>> groupedEntry : groupedFilterableNotifications.entrySet()) {
            FilteredDistributionJobResponseModel filteredJob = groupedEntry.getKey();
            FilteredJobNotificationWrapper wrappedJobNotifications = new FilteredJobNotificationWrapper(filteredJob.getId(), filteredJob.getProcessingType(), filteredJob.getChannelName(), groupedEntry.getValue());
            filterableJobNotifications.add(wrappedJobNotifications);
        }

        return new AlertPagedModel<>(jobs.getTotalPages(), pageNumber, pageSize, filterableJobNotifications);
    }
}
