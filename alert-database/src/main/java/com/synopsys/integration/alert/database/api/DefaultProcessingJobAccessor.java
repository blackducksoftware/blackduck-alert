/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingJobAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModelV2;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobResponseModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.database.job.DistributionJobEntity;
import com.synopsys.integration.alert.database.job.DistributionJobRepository;
import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsEntity;

@Component
public class DefaultProcessingJobAccessor implements ProcessingJobAccessor {
    private final DistributionJobRepository distributionJobRepository;
    private final BlackDuckJobDetailsAccessor blackDuckJobDetailsAccessor;

    @Autowired
    public DefaultProcessingJobAccessor(DistributionJobRepository distributionJobRepository, BlackDuckJobDetailsAccessor blackDuckJobDetailsAccessor) {
        this.distributionJobRepository = distributionJobRepository;
        this.blackDuckJobDetailsAccessor = blackDuckJobDetailsAccessor;
    }

    @Override
    public AlertPagedModel<FilteredDistributionJobResponseModel> getMatchingEnabledJobsByFilteredNotifications(FilteredDistributionJobRequestModelV2 filteredDistributionJobRequestModel, int pageNumber, int pageLimit) {
        List<String> frequencyTypes = filteredDistributionJobRequestModel.getFrequencyTypes()
                                          .stream()
                                          .map(Enum::name)
                                          .collect(Collectors.toList());

        Set<String> projectNames = filteredDistributionJobRequestModel.getProjectName();
        Set<String> notificationTypes = filteredDistributionJobRequestModel.getNotificationTypes();

        // If no policies and/or vulnerabilitySeverities exist the repository query expects a null to be passed
        Set<String> policyNames = filteredDistributionJobRequestModel.getPolicyNames().orElse(null);
        Set<String> vulnerabilitySeverities = filteredDistributionJobRequestModel.getVulnerabilitySeverities().orElse(null);

        PageRequest pageRequest = PageRequest.of(pageNumber, pageLimit);
        Page<DistributionJobEntity> pageOfDistributionJobEntities;

        pageOfDistributionJobEntities = distributionJobRepository.findMatchingEnabledJobsByFilteredNotifications(frequencyTypes, notificationTypes, projectNames, policyNames, vulnerabilitySeverities, pageRequest);

        List<FilteredDistributionJobResponseModel> distributionJobResponseModels = pageOfDistributionJobEntities.getContent()
                                                                                       .stream()
                                                                                       .map(this::convertToFilteredDistributionJobResponseModel)
                                                                                       .collect(Collectors.toList());
        return new AlertPagedModel<>(pageOfDistributionJobEntities.getTotalPages(), pageNumber, pageLimit, distributionJobResponseModels);

    }

    private FilteredDistributionJobResponseModel convertToFilteredDistributionJobResponseModel(DistributionJobEntity jobEntity) {
        UUID jobId = jobEntity.getJobId();
        ProcessingType processingType = Enum.valueOf(ProcessingType.class, jobEntity.getProcessingType());
        String channelName = jobEntity.getChannelDescriptorName();

        BlackDuckJobDetailsEntity blackDuckJobDetails = jobEntity.getBlackDuckJobDetails();
        List<String> notificationTypes = blackDuckJobDetailsAccessor.retrieveNotificationTypesForJob(jobId);
        List<BlackDuckProjectDetailsModel> projectDetails = blackDuckJobDetailsAccessor.retrieveProjectDetailsForJob(jobId);
        List<String> policyNames = blackDuckJobDetailsAccessor.retrievePolicyNamesForJob(jobId);
        List<String> vulnerabilitySeverityNames = blackDuckJobDetailsAccessor.retrieveVulnerabilitySeverityNamesForJob(jobId);

        boolean filterByProject = blackDuckJobDetails.getFilterByProject();
        String projectNamePattern = blackDuckJobDetails.getProjectNamePattern();

        return new FilteredDistributionJobResponseModel(
            jobId,
            processingType,
            channelName,
            notificationTypes,
            projectDetails,
            policyNames,
            vulnerabilitySeverityNames,
            filterByProject,
            projectNamePattern
        );
    }
}

