/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.mapping;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.processor.detail.DetailedNotificationContent;
import com.blackduck.integration.alert.api.processor.filter.JobNotificationFilterUtils;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.persistence.accessor.JobNotificationMappingAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.ProcessingJobAccessor2;
import com.blackduck.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModel;
import com.blackduck.integration.alert.common.persistence.model.job.JobToNotificationMappingModel;
import com.blackduck.integration.alert.common.persistence.model.job.SimpleFilteredDistributionJobResponseModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedDetails;

@Component
public class JobNotificationMapper2 {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ProcessingJobAccessor2 processingJobAccessor;
    private final JobNotificationMappingAccessor jobNotificationMappingAccessor;

    @Autowired
    public JobNotificationMapper2(
        ProcessingJobAccessor2 processingJobAccessor,
        JobNotificationMappingAccessor jobNotificationMappingAccessor
    ) {
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
            .forEach(jobRequestModel -> retrieveResponse(correlationID, jobRequestModel));
    }

    public boolean hasBatchReachedSizeLimit(UUID correlationID, int limit) {
        return limit < jobNotificationMappingAccessor.getCountByCorrelationId(correlationID);
    }

    private FilteredDistributionJobRequestModel convertToRequest(DetailedNotificationContent detailedNotificationContent, List<FrequencyType> frequencies) {
        FilteredDistributionJobRequestModel filteredDistributionJobRequestModel = new FilteredDistributionJobRequestModel(
            detailedNotificationContent.getProviderConfigId(),
            detailedNotificationContent.getNotificationContentWrapper().getNotificationId(),
            frequencies
        );
        detailedNotificationContent.getProjectName().ifPresent(filteredDistributionJobRequestModel::addProjectName);
        detailedNotificationContent.getProjectVersionName().ifPresent(filteredDistributionJobRequestModel::addProjectVersionName);
        filteredDistributionJobRequestModel.addNotificationType(detailedNotificationContent.getNotificationContentWrapper().extractNotificationType());
        filteredDistributionJobRequestModel.addVulnerabilitySeverities(detailedNotificationContent.getVulnerabilitySeverities());
        detailedNotificationContent.getPolicyName().ifPresent(filteredDistributionJobRequestModel::addPolicyName);
        return filteredDistributionJobRequestModel;
    }

    private void retrieveResponse(
        UUID correlationId,
        FilteredDistributionJobRequestModel filteredDistributionJobRequestModel
    ) {
        int pageNumber = 0;
        int pageSize = 200;

        String projectName = filteredDistributionJobRequestModel.getProjectName().stream().findFirst().orElse(StringUtils.EMPTY);
        String projectVersionName = filteredDistributionJobRequestModel.getProjectVersionNames().stream().findFirst().orElse(StringUtils.EMPTY);

        AlertPagedDetails<SimpleFilteredDistributionJobResponseModel> jobs = processingJobAccessor.getMatchingEnabledJobsForNotifications(
            filteredDistributionJobRequestModel,
            pageNumber,
            pageSize
        );
        // TODO: Investigate reasoning for usage of Optional in FilteredDistributionJobRequestModel.notificationId
        Long notificationId = filteredDistributionJobRequestModel.getNotificationId().orElse(0L);
        logger.debug("Beginning job to notification mapping for correlationId: [{}] for notification: [{}]", correlationId, notificationId);
        boolean doAnyJobsApply = false;
        while (jobs.getCurrentPage() <= jobs.getTotalPages()) {
            List<JobToNotificationMappingModel> mappings = new LinkedList<>();
            for (SimpleFilteredDistributionJobResponseModel job : jobs.getModels()) {
                if (JobNotificationFilterUtils.doesProjectApplyToJob(job, projectName, projectVersionName)) {
                    doAnyJobsApply = true;
                    mappings.add(new JobToNotificationMappingModel(correlationId, job.getJobId(), job.getNotificationId()));
                }
            }
            if (!mappings.isEmpty()) {
                for (JobToNotificationMappingModel mapping : mappings) {
                    logger.debug("Mapping jobId: [{}] to notificationId: [{}] for correlationId: [{}]", mapping.getJobId(), mapping.getNotificationId(), mapping.getCorrelationId());
                }
                jobNotificationMappingAccessor.addJobMappings(mappings);
            }
            pageNumber++;
            jobs = processingJobAccessor.getMatchingEnabledJobsForNotifications(
                filteredDistributionJobRequestModel,
                pageNumber,
                pageSize
            );
        }
        if (!doAnyJobsApply) {
            logger.debug("No jobs to notification mapping for notification: [{}]", notificationId);
        }
        logger.debug("Completed job to notification mapping for correlationId: [{}] for notification: [{}]", correlationId, notificationId);
    }
}
