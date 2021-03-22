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
    public StatefulAlertPagedModel<FilteredJobNotificationWrapper> mapJobsToNotifications(List<DetailedNotificationContent> detailedContents, Collection<FrequencyType> frequencies) {
        AlertPagedModel<FilteredJobNotificationWrapper> pageOfJobsToNotification = mapPageOfJobsToNotification(detailedContents, frequencies, PAGE_NUMBER, PAGE_SIZE);
        //TODO rename size/number
        return new StatefulAlertPagedModel<>(
            PAGE_SIZE,
            PAGE_NUMBER,
            pageOfJobsToNotification.getModels(),
            (size, number) -> mapPageOfJobsToNotification(detailedContents, frequencies, number, size)
        );
    }

    private AlertPagedModel<FilteredJobNotificationWrapper> mapPageOfJobsToNotification(List<DetailedNotificationContent> detailedContents, Collection<FrequencyType> frequencies, int pageNumber, int pageSize) {
        Map<FilteredDistributionJobResponseModel, List<NotificationContentWrapper>> groupedFilterableNotifications = new HashMap<>();
        int totalPages = 0; //What is the best way to get total pages? What if one notificationContent has more pages of Jobs than another? Perhaps get the highest total page?
        //int currentPage = 0;

        for (DetailedNotificationContent detailedNotificationContent : detailedContents) {
            AlertPagedModel<FilteredDistributionJobResponseModel> filteredDistributionJobResponseModels = retrieveMatchingJobs(detailedNotificationContent, frequencies, pageNumber, pageSize);
            //get totalPages and currentPage from the alertPagedModel here
            //FIXME? if we loop over all the detailedContents, these values might change. Need to debug and see what happens here
            //totalPages = filteredDistributionJobResponseModels.getTotalPages(); //we should accumulate instead totalPages += ?
            //currentPage = filteredDistributionJobResponseModels.getCurrentPage();
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

}
