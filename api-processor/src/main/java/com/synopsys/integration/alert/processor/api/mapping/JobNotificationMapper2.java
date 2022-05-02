/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.mapping;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingJobAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobResponseModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedDetails;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.processor.api.filter.JobNotificationFilterUtils;

@Component
public class JobNotificationMapper2 {
    private final JobNotificationMap jobNotificationMap;
    private final ProcessingJobAccessor processingJobAccessor;

    public JobNotificationMapper2(JobNotificationMap jobNotificationMap, ProcessingJobAccessor processingJobAccessor) {
        this.jobNotificationMap = jobNotificationMap;
        this.processingJobAccessor = processingJobAccessor;
    }

    public void mapJobsToNotifications(
        UUID correlationID,
        List<DetailedNotificationContent> detailedContents,
        List<FrequencyType> frequencies
    ) {
        detailedContents
            .stream()
            .map(content -> convertToRequest(content, frequencies))
            .forEach(jobRequestModel -> retrieveResponse(correlationID, jobRequestModel, detailedContents));
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

    private void retrieveResponse(
        UUID correlationId,
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
        while (jobs.getCurrentPage() <= jobs.getTotalPages()) {
            for (FilteredDistributionJobResponseModel job : jobs.getModels()) {
                for (DetailedNotificationContent notificationContent : detailedNotificationContents) {
                    if (JobNotificationFilterUtils.doesNotificationApplyToJob(job, notificationContent)
                        && notificationContent.getProviderConfigId().equals(filteredDistributionJobRequestModel.getProviderConfigId())) {
                        jobNotificationMap.addMapping(correlationId, job.getId(), notificationContent.getNotificationContentWrapper().getNotificationId());
                    }
                }
            }

            pageNumber++;
            jobs = processingJobAccessor.getMatchingEnabledJobsByFilteredNotifications(
                filteredDistributionJobRequestModel,
                pageNumber,
                pageSize
            );
        }
    }
}
