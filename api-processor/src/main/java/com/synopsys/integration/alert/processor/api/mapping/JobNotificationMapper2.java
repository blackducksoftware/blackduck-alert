/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.mapping;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.persistence.accessor.JobNotificationMappingAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingJobAccessor2;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.JobToNotificationMappingModel;
import com.synopsys.integration.alert.common.persistence.model.job.SimpleFilteredDistributionJobResponseModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedDetails;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;

@Component
public class JobNotificationMapper2 {
    private final JobNotificationMap jobNotificationMap;
    private final ProcessingJobAccessor2 processingJobAccessor;
    private final JobNotificationMappingAccessor jobNotificationMappingAccessor;

    @Autowired
    public JobNotificationMapper2(
        JobNotificationMap jobNotificationMap,
        ProcessingJobAccessor2 processingJobAccessor,
        JobNotificationMappingAccessor jobNotificationMappingAccessor
    ) {
        this.jobNotificationMap = jobNotificationMap;
        this.processingJobAccessor = processingJobAccessor;
        this.jobNotificationMappingAccessor = jobNotificationMappingAccessor;
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
            detailedNotificationContent.getNotificationContentWrapper().getNotificationId(),
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
        AlertPagedDetails<SimpleFilteredDistributionJobResponseModel> jobs = processingJobAccessor.getMatchingEnabledJobsForNotifications(
            filteredDistributionJobRequestModel,
            pageNumber,
            pageSize
        );
        while (jobs.getCurrentPage() <= jobs.getTotalPages()) {
            List<JobToNotificationMappingModel> mappings = new LinkedList<>();
            for (SimpleFilteredDistributionJobResponseModel job : jobs.getModels()) {
                mappings.add(new JobToNotificationMappingModel(correlationId, job.getJobId(), job.getNotificationId()));
            }
            if (!mappings.isEmpty()) {
                jobNotificationMappingAccessor.addJobMappings(mappings);
            }
            pageNumber++;
            jobs = processingJobAccessor.getMatchingEnabledJobsForNotifications(
                filteredDistributionJobRequestModel,
                pageNumber,
                pageSize
            );
        }
    }
}
