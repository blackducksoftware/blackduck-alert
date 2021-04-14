/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.filter;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * @return a {@code Map} where the distribution job is used to map to a list of notifications that were passed in.
     */
    //TODO, this might be better renamed to something like mapJobsToFirstPageOfNotifications? (It default starts at page 0, size 100)
    public StatefulAlertPagedModel<FilteredJobNotificationWrapper> mapJobsToNotifications(List<DetailedNotificationContent> detailedContents, Collection<FrequencyType> frequencies) {
        AlertPagedModel<FilteredJobNotificationWrapper> pageOfJobsToNotification = mapPageOfJobsToNotification(detailedContents, frequencies, INITIAL_PAGE_NUMBER, PAGE_SIZE);
        return new StatefulAlertPagedModel<>(
            pageOfJobsToNotification.getTotalPages(),
            pageOfJobsToNotification.getCurrentPage(),
            pageOfJobsToNotification.getPageSize(),
            pageOfJobsToNotification.getModels(),
            (number, size) -> mapPageOfJobsToNotification(detailedContents, frequencies, number, size)
        );
    }

    private AlertPagedModel<FilteredJobNotificationWrapper> mapPageOfJobsToNotification(List<DetailedNotificationContent> detailedContents, Collection<FrequencyType> frequencies, int pageNumber, int pageSize) {
        //Step1: Iterate over notifications and collect Sets of notification Types, vuln. severities, & policy names
        //Step2: Create and execute a query to get a page of jobs matching any notificationTypes, severities, or policy names (from step 1).
        //          If Policy Notification Type Filter or Vulnerability Notification Type Filer are null, then all notifications match
        //Step3: Take 1 job and compare to all notifications in memory NOTE: We should create a new class to do this
        //Step4: Create and return the FilteredJobNotificationWrappers and page number of the query we are doing

        Map<FilteredDistributionJobResponseModel, List<NotificationContentWrapper>> groupedFilterableNotifications = new HashMap<>();

        //Step1:
        //filtered might not be the best word to use, perhaps something like matchedNotifications?
        Set<String> filteredProjectNames = new HashSet<>();
        Set<String> filteredNotificationTypes = new HashSet<>();
        Set<String> filteredVulnerabilitySeverities = new HashSet<>();
        Set<String> filteredPolicyNames = new HashSet<>();
        for (DetailedNotificationContent detailedNotificationContent : detailedContents) {
            detailedNotificationContent.getProjectName().ifPresent(filteredProjectNames::add);
            filteredNotificationTypes.add(detailedNotificationContent.getNotificationContentWrapper().extractNotificationType());
            filteredVulnerabilitySeverities.addAll(detailedNotificationContent.getVulnerabilitySeverities());
            detailedNotificationContent.getPolicyName().ifPresent(filteredPolicyNames::add);
        }

        //Step2: get matching jobs using the filteredNotifications above
        //Note: I can probably move this to its own private method later
        FilteredDistributionJobRequestModel filteredDistributionJobRequestModel = new FilteredDistributionJobRequestModel(
            frequencies,
            filteredProjectNames,
            filteredNotificationTypes,
            filteredVulnerabilitySeverities,
            filteredPolicyNames
        );
        AlertPagedModel<FilteredDistributionJobResponseModel> jobs = processingJobAccessor.getMatchingEnabledJobsByFilteredNotifications(filteredDistributionJobRequestModel, pageNumber, pageSize);

        //Step3:
        //possibly also break this into its own method. We need some kind of check here to make sure that jobs != 0, most likely need to skip it.
        // throw a debug message that notifications discovered but no jobs were mapped?
        // otherwise this will fail in retreieveMachingEventsByJobIds

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
