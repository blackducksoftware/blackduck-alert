/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.filter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingJobAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobResponseModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedDetails;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;

@Component
public class JobNotificationMapper {
    private static final int PAGE_SIZE = 100;
    private static final int INITIAL_PAGE_NUMBER = 0;
    private static final Predicate<AlertPagedDetails> HAS_NEXT_PAGE = page -> page.getCurrentPage() < (page.getTotalPages() - 1);

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
     * @return a {@code StatefulAlertPage} where a page of distribution jobs is used to map to a list of notifications that were passed in.
     */
    public StatefulAlertPage<FilteredJobNotificationWrapper, RuntimeException> mapJobsToNotifications(List<DetailedNotificationContent> detailedContents, List<FrequencyType> frequencies) {
        FilteredJobWrapperPageRetriever filteredJobWrapperPageRetriever = new FilteredJobWrapperPageRetriever(detailedContents, frequencies);
        AlertPagedDetails<FilteredJobNotificationWrapper> firstPage = filteredJobWrapperPageRetriever.retrievePage(INITIAL_PAGE_NUMBER, PAGE_SIZE);
        return new StatefulAlertPage<>(firstPage, filteredJobWrapperPageRetriever, HAS_NEXT_PAGE);
    }

    private AlertPagedDetails<FilteredJobNotificationWrapper> mapPageOfJobsToNotification(List<DetailedNotificationContent> detailedContents, List<FrequencyType> frequencies, int pageNumber, int pageSize) {
        if (detailedContents.isEmpty()) {
            return new AlertPagedDetails<>(1, pageNumber, pageSize, List.of());
        }

        Map<FilteredDistributionJobResponseModel, List<NotificationContentWrapper>> groupedFilterableNotifications = new HashMap<>();

        FilteredDistributionJobRequestModel filteredDistributionJobRequestModel = createRequestModelFromNotifications(detailedContents, frequencies);

        AlertPagedDetails<FilteredDistributionJobResponseModel> jobs = processingJobAccessor.getMatchingEnabledJobsByFilteredNotifications(filteredDistributionJobRequestModel, pageNumber, pageSize);
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
            FilteredJobNotificationWrapper wrappedJobNotifications = new FilteredJobNotificationWrapper(filteredJob.getId(), filteredJob.getProcessingType(), filteredJob.getChannelName(), filteredJob.getJobName(), groupedEntry.getValue());
            filterableJobNotifications.add(wrappedJobNotifications);
        }

        return new AlertPagedDetails<>(jobs.getTotalPages(), pageNumber, pageSize, filterableJobNotifications);
    }

    private class FilteredJobWrapperPageRetriever implements PageRetriever<FilteredJobNotificationWrapper, RuntimeException> {
        private final List<DetailedNotificationContent> detailedContents;
        private final List<FrequencyType> frequencies;

        public FilteredJobWrapperPageRetriever(List<DetailedNotificationContent> detailedContents, List<FrequencyType> frequencies) {
            this.detailedContents = detailedContents;
            this.frequencies = frequencies;
        }

        @Override
        public AlertPagedDetails<FilteredJobNotificationWrapper> retrieveNextPage(int currentOffset, int currentLimit) throws RuntimeException {
            return retrievePage(currentOffset + 1, currentLimit);
        }

        @Override
        public AlertPagedDetails<FilteredJobNotificationWrapper> retrievePage(int currentOffset, int currentLimit) throws RuntimeException {
            return mapPageOfJobsToNotification(detailedContents, frequencies, currentOffset, currentLimit);
        }

    }

    private FilteredDistributionJobRequestModel createRequestModelFromNotifications(List<DetailedNotificationContent> detailedContents, List<FrequencyType> frequencies) {
        Long commonProviderConfigId = detailedContents
            .stream()
            .map(DetailedNotificationContent::getProviderConfigId)
            .findAny()
            .orElseThrow(() -> new AlertRuntimeException("Notification(s) missing provider configuration id"));
        FilteredDistributionJobRequestModel filteredDistributionJobRequestModel = new FilteredDistributionJobRequestModel(commonProviderConfigId, frequencies);
        for (DetailedNotificationContent detailedNotificationContent : detailedContents) {
            detailedNotificationContent.getProjectName().ifPresent(filteredDistributionJobRequestModel::addProjectName);
            filteredDistributionJobRequestModel.addNotificationType(detailedNotificationContent.getNotificationContentWrapper().extractNotificationType());
            filteredDistributionJobRequestModel.addVulnerabilitySeverities(detailedNotificationContent.getVulnerabilitySeverities());
            detailedNotificationContent.getPolicyName().ifPresent(filteredDistributionJobRequestModel::addPolicyName);
        }
        return filteredDistributionJobRequestModel;
    }

}
