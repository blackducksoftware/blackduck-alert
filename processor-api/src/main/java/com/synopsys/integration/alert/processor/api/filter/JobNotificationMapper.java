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
import java.util.UUID;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModelV2;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobResponseModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@Component
public class JobNotificationMapper {
    private static final int PAGE_SIZE = 10;
    private static final int PAGE_NUMBER = 0;

    private final JobAccessor jobAccessor;

    @Autowired
    public JobNotificationMapper(JobAccessor jobAccessor) {
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
     * @param detailedContents List of notifications that will be iterated over and applied to jobs that are found
     * @param frequencies      an Additional filter to specify when querying data from the DB
     * @return a {@code Map} where the distribution job is used to map to a list of notifications that were passed in.
     */
    //TODO, this might be better renamed to something like mapJobsToFirstPageOfNotifications? (It default starts at page 0, size 100)
    public StatefulAlertPagedModel<FilteredJobNotificationWrapper> mapJobsToNotifications(List<DetailedNotificationContent> detailedContents, Collection<FrequencyType> frequencies) {
        AlertPagedModel<FilteredJobNotificationWrapper> pageOfJobsToNotification = mapPageOfJobsToNotification(detailedContents, frequencies, PAGE_NUMBER, PAGE_SIZE);
        return new StatefulAlertPagedModel<>(
            pageOfJobsToNotification.getCurrentPage(),
            pageOfJobsToNotification.getPageSize(),
            pageOfJobsToNotification.getModels(),
            (number, size) -> mapPageOfJobsToNotification(detailedContents, frequencies, number, size)
        );
    }

    //TODO:
    private AlertPagedModel<FilteredJobNotificationWrapper> mapPageOfJobsToNotification(List<DetailedNotificationContent> detailedContents, Collection<FrequencyType> frequencies, int pageNumber, int pageSize) {

        //Step1: Iterate over notifications and collect Sets of notification Types, vuln. severities, & policy names
        //Step2: Create and execute a query to get a page of jobs matching any notificationTypes, severities, or policy names (from step 1).
        //          If Policy Notification Type Filter or Vulnerability Notification Type Filer are null, then all notifications match
        //Step3: Take 1 job and compare to all notifications in memory NOTE: We should create a new class to do this
        //Step4: Create and return the FilteredJobNotificationWrappers and page number of the query we are doing

        Map<FilteredDistributionJobResponseModel, List<NotificationContentWrapper>> groupedFilterableNotifications = new HashMap<>();
        int totalPages = 0;

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
        FilteredDistributionJobRequestModelV2 filteredDistributionJobRequestModel = new FilteredDistributionJobRequestModelV2(
            frequencies,
            filteredProjectNames,
            filteredNotificationTypes,
            filteredVulnerabilitySeverities,
            filteredPolicyNames
        );
        AlertPagedModel<FilteredDistributionJobResponseModel> jobs = jobAccessor.getMatchingEnabledJobsByFilteredNotifications(filteredDistributionJobRequestModel, pageNumber, pageSize);

        //Step3:
        for (DetailedNotificationContent detailedNotificationContent : detailedContents) {
            for (FilteredDistributionJobResponseModel filteredDistributionJobResponseModel : jobs.getModels()) {
                List<NotificationContentWrapper> applicableNotifications = groupedFilterableNotifications.computeIfAbsent(filteredDistributionJobResponseModel, ignoredKey -> new LinkedList<>());
                applicableNotifications.add(detailedNotificationContent.getNotificationContentWrapper());
            }
        }

        List<FilteredJobNotificationWrapper> filterableJobNotifications = new LinkedList<>();
        for (Map.Entry<FilteredDistributionJobResponseModel, List<NotificationContentWrapper>> groupedEntry : groupedFilterableNotifications.entrySet()) {
            FilteredDistributionJobResponseModel filteredJob = groupedEntry.getKey();
            FilteredJobNotificationWrapper wrappedJobNotifications = new FilteredJobNotificationWrapper(filteredJob.getId(), filteredJob.getProcessingType(), filteredJob.getChannelName(), groupedEntry.getValue());
            filterableJobNotifications.add(wrappedJobNotifications);
        }

        return new AlertPagedModel<>(totalPages, pageNumber, pageSize, filterableJobNotifications);

        //Old implementation:
        /*
        for (DetailedNotificationContent detailedNotificationContent : detailedContents) {
            AlertPagedModel<FilteredDistributionJobResponseModel> filteredDistributionJobResponseModels;
            //If there are grouped notifications already saved, then look for only id's that match those notifications.
            if (groupedFilterableNotifications.size() > 0) {
                List<UUID> uuids = new ArrayList<>();
                for (Map.Entry<FilteredDistributionJobResponseModel, List<NotificationContentWrapper>> groupedEntry : groupedFilterableNotifications.entrySet()) {
                    uuids.add(groupedEntry.getKey().getId());
                }
                //NOTE: pageNumber needs to be tracked differently when calling this. On a second run through the pageNumber will be incorrect (should be 0, actually page 1)
                //pageNumber = 0;
                filteredDistributionJobResponseModels = retrieveMatchingEventsByJobIds(detailedNotificationContent, frequencies, uuids, 0, pageSize);
                //differentiate between both types of Models to determine total pages?
                if (filteredDistributionJobResponseModels.getTotalPages() > 1) {
                    System.out.println(); //FIXME: If it gets to this point, I have a problem
                }
            } else { //If there are no jobs, get (up to) the next 10 jobs
                filteredDistributionJobResponseModels = retrieveMatchingJobs(detailedNotificationContent, frequencies, pageNumber, pageSize);
                //initial total page number
            }
            totalPages = Math.max(totalPages, filteredDistributionJobResponseModels.getTotalPages());

            for (FilteredDistributionJobResponseModel filteredDistributionJobResponseModel : filteredDistributionJobResponseModels.getModels()) {
                List<NotificationContentWrapper> applicableNotifications = groupedFilterableNotifications.computeIfAbsent(filteredDistributionJobResponseModel, ignoredKey -> new LinkedList<>());
                applicableNotifications.add(detailedNotificationContent.getNotificationContentWrapper());
            }
        }

        List<FilteredJobNotificationWrapper> filterableJobNotifications = new LinkedList<>();
        for (Map.Entry<FilteredDistributionJobResponseModel, List<NotificationContentWrapper>> groupedEntry : groupedFilterableNotifications.entrySet()) {
            FilteredDistributionJobResponseModel filteredJob = groupedEntry.getKey();
            FilteredJobNotificationWrapper wrappedJobNotifications = new FilteredJobNotificationWrapper(filteredJob.getId(), filteredJob.getProcessingType(), filteredJob.getChannelName(), groupedEntry.getValue());
            filterableJobNotifications.add(wrappedJobNotifications);
        }

        return new AlertPagedModel<>(totalPages, pageNumber, pageSize, filterableJobNotifications);

         */
    }

    /*
    private AlertPagedModel<FilteredJobNotificationWrapper> mapPageOfJobsToNotification(List<DetailedNotificationContent> detailedContents, Collection<FrequencyType> frequencies, int pageNumber, int pageSize) {
        Map<FilteredDistributionJobResponseModel, List<NotificationContentWrapper>> groupedFilterableNotifications = new HashMap<>();
        int totalPages = 0; //What is the best way to get total pages? What if one notificationContent has more pages of Jobs than another? Perhaps get the highest total page?

        //Go over each notification, and find all jobs that map to it, saving them to memory
        for (DetailedNotificationContent detailedNotificationContent : detailedContents) {
            AlertPagedModel<FilteredDistributionJobResponseModel> filteredDistributionJobResponseModels = retrieveMatchingJobs(detailedNotificationContent, frequencies, pageNumber, pageSize);
            //Find the DetailedNotificationContent with the most pages and set it to totalPages
            totalPages = Math.max(totalPages, filteredDistributionJobResponseModels.getTotalPages());

            //Loop over the list of jobs, and add them to the groupedFilterableNotifications map. Then, take the notificationContentWrapper from the notificaitonContent and add it to the map as a value in List form.
            for (FilteredDistributionJobResponseModel filteredDistributionJobResponseModel : filteredDistributionJobResponseModels.getModels()) {
                List<NotificationContentWrapper> applicableNotifications = groupedFilterableNotifications.computeIfAbsent(filteredDistributionJobResponseModel, ignoredKey -> new LinkedList<>());
                applicableNotifications.add(detailedNotificationContent.getNotificationContentWrapper());
            }
        }

        //TODO: Important: verify if the above list is always the same size as this list here since it's used to determine the size of the next pagedModel
        List<FilteredJobNotificationWrapper> filterableJobNotifications = new LinkedList<>();
        for (Map.Entry<FilteredDistributionJobResponseModel, List<NotificationContentWrapper>> groupedEntry : groupedFilterableNotifications.entrySet()) {
            FilteredDistributionJobResponseModel filteredJob = groupedEntry.getKey();
            FilteredJobNotificationWrapper wrappedJobNotifications = new FilteredJobNotificationWrapper(filteredJob.getId(), filteredJob.getProcessingType(), filteredJob.getChannelName(), groupedEntry.getValue());
            filterableJobNotifications.add(wrappedJobNotifications);
        }

        //TODO: fill in the requirements for AlertPagedModel
        return new AlertPagedModel<>(totalPages, pageNumber, pageSize, filterableJobNotifications);
    }
    */

    //WIP: see if we can use this to get the total number of pages for each notification
    //TODO, if this doesn't get used, delete it
    private Map<DetailedNotificationContent, Integer> getPageTotals(List<DetailedNotificationContent> detailedContents, Collection<FrequencyType> frequencies, int pageNumber, int pageSize) {
        Map<DetailedNotificationContent, Integer> pageTotalsOfNotifications = new HashMap<>();

        for (DetailedNotificationContent detailedNotificationContent : detailedContents) {
            AlertPagedModel<FilteredDistributionJobResponseModel> filteredDistributionJobResponseModels = retrieveMatchingJobs(detailedNotificationContent, frequencies, pageNumber, pageSize);
            pageTotalsOfNotifications.put(detailedNotificationContent, filteredDistributionJobResponseModels.getTotalPages());
        }
        return pageTotalsOfNotifications;
    }

    //Get the jobs that map to the single notification passed in by detailedNotificationContent
    private AlertPagedModel<FilteredDistributionJobResponseModel> retrieveMatchingJobs(DetailedNotificationContent detailedNotificationContent, Collection<FrequencyType> frequencyTypes, int pageNumber, int pageSize) {
        NotificationContentWrapper contentWrapper = detailedNotificationContent.getNotificationContentWrapper();
        FilteredDistributionJobRequestModel filteredDistributionJobRequestModel = new FilteredDistributionJobRequestModel(
            frequencyTypes,
            EnumUtils.getEnum(NotificationType.class, contentWrapper.extractNotificationType()),
            detailedNotificationContent.getProjectName().orElse(null),
            detailedNotificationContent.getVulnerabilitySeverities(),
            detailedNotificationContent.getPolicyName().map(List::of).orElse(List.of())
        );
        //TODO: It might be better to return the FilteredDistributionJobRequestModel instead and run the jobAccessor elsewhere.
        return jobAccessor.getMatchingEnabledJobs(filteredDistributionJobRequestModel, pageNumber, pageSize);
    }

    private AlertPagedModel<FilteredDistributionJobResponseModel> retrieveMatchingEventsByJobIds(DetailedNotificationContent detailedNotificationContent, Collection<FrequencyType> frequencyTypes, List<UUID> jobIds, int pageNumber,
        int pageSize) {
        NotificationContentWrapper contentWrapper = detailedNotificationContent.getNotificationContentWrapper();
        FilteredDistributionJobRequestModel filteredDistributionJobRequestModel = new FilteredDistributionJobRequestModel(
            frequencyTypes,
            EnumUtils.getEnum(NotificationType.class, contentWrapper.extractNotificationType()),
            detailedNotificationContent.getProjectName().orElse(null),
            detailedNotificationContent.getVulnerabilitySeverities(),
            detailedNotificationContent.getPolicyName().map(List::of).orElse(List.of())
        );
        return jobAccessor.getMatchingEnabledJobsByJobIds(filteredDistributionJobRequestModel, jobIds, pageNumber, pageSize);
    }

}
