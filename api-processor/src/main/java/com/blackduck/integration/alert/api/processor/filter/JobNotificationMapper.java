/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.filter;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.processor.detail.DetailedNotificationContent;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.persistence.accessor.ProcessingJobAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModel;
import com.blackduck.integration.alert.common.persistence.model.job.FilteredDistributionJobResponseModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedDetails;
import com.blackduck.integration.util.Stringable;

@Component
public class JobNotificationMapper {
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
     */
    public Set<FilteredJobNotificationWrapper> mapJobsToNotifications(
        List<DetailedNotificationContent> detailedContents,
        List<FrequencyType> frequencies
    ) {
        return detailedContents
            .stream()
            .map(content -> convertToRequest(content, frequencies))
            .map(jobRequestModel -> retrieveResponse(jobRequestModel, detailedContents))
            .flatMap(Set::stream)
            .map(this::convertToWrapper)
            .collect(Collectors.toSet());
    }

    private FilteredDistributionJobRequestModel convertToRequest(DetailedNotificationContent detailedNotificationContent, List<FrequencyType> frequencies) {
        FilteredDistributionJobRequestModel filteredDistributionJobRequestModel = new FilteredDistributionJobRequestModel(
            detailedNotificationContent.getProviderConfigId(),
            frequencies
        );
        detailedNotificationContent.getProjectName().ifPresent(filteredDistributionJobRequestModel::addProjectName);
        filteredDistributionJobRequestModel.addNotificationType(detailedNotificationContent.getNotificationContentWrapper().extractNotificationType());
        filteredDistributionJobRequestModel.addVulnerabilitySeverities(detailedNotificationContent.getVulnerabilitySeverities());
        detailedNotificationContent.getPolicyName().ifPresent(filteredDistributionJobRequestModel::addPolicyName);
        return filteredDistributionJobRequestModel;
    }

    private Set<JobWithNotifications> retrieveResponse(
        FilteredDistributionJobRequestModel filteredDistributionJobRequestModel,
        List<DetailedNotificationContent> detailedNotificationContents
    ) {
        int pageNumber = 0;
        int pageSize = 1000;
        AlertPagedDetails<FilteredDistributionJobResponseModel> jobs = processingJobAccessor.getMatchingEnabledJobsByFilteredNotifications(
            filteredDistributionJobRequestModel,
            pageNumber,
            pageSize
        );
        Set<JobWithNotifications> jobWithNotifications = new HashSet<>();
        while (jobs.getCurrentPage() <= jobs.getTotalPages()) {
            jobs.getModels().stream()
                .map(JobWithNotifications::new)
                .map(jobWithNotification -> jobWithNotification.addNotificationsIfApplicable(
                    filteredDistributionJobRequestModel.getProviderConfigId(),
                    detailedNotificationContents
                ))
                .filter(JobWithNotifications::hasRelevantNotifications)
                .forEach(jobWithNotifications::add);

            pageNumber++;
            jobs = processingJobAccessor.getMatchingEnabledJobsByFilteredNotifications(
                filteredDistributionJobRequestModel,
                pageNumber,
                pageSize
            );
        }

        return jobWithNotifications;
    }

    private FilteredJobNotificationWrapper convertToWrapper(JobWithNotifications jobWithNotifications) {
        FilteredDistributionJobResponseModel filteredDistributionJobResponseModel = jobWithNotifications.getFilteredDistributionJobResponseModel();
        return new FilteredJobNotificationWrapper(
            filteredDistributionJobResponseModel.getId(),
            filteredDistributionJobResponseModel.getProcessingType(),
            filteredDistributionJobResponseModel.getChannelName(),
            filteredDistributionJobResponseModel.getJobName(),
            jobWithNotifications.getNotificationContentWrappers()
        );
    }

    private class JobWithNotifications extends Stringable {
        private FilteredDistributionJobResponseModel filteredDistributionJobResponseModel;
        private List<NotificationContentWrapper> notificationContentWrappers = new LinkedList<>();

        public JobWithNotifications(
            FilteredDistributionJobResponseModel filteredDistributionJobResponseModel
        ) {
            this.filteredDistributionJobResponseModel = filteredDistributionJobResponseModel;
        }

        public FilteredDistributionJobResponseModel getFilteredDistributionJobResponseModel() {
            return filteredDistributionJobResponseModel;
        }

        public List<NotificationContentWrapper> getNotificationContentWrappers() {
            return notificationContentWrappers;
        }

        public JobWithNotifications addNotificationsIfApplicable(Long providerId, List<DetailedNotificationContent> detailedNotificationContents) {
            for (DetailedNotificationContent detailedNotificationContent : detailedNotificationContents) {
                if (JobNotificationFilterUtils.doesNotificationApplyToJob(filteredDistributionJobResponseModel, detailedNotificationContent)
                    && detailedNotificationContent.getProviderConfigId().equals(providerId)) {
                    notificationContentWrappers.add(detailedNotificationContent.getNotificationContentWrapper());
                }
            }
            return this;
        }

        public boolean hasRelevantNotifications() {
            return !notificationContentWrappers.isEmpty();
        }
    }

}
