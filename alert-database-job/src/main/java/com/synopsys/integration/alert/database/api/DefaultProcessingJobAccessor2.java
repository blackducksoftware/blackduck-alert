package com.synopsys.integration.alert.database.api;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.accessor.ProcessingJobAccessor2;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.SimpleFilteredDistributionJobResponseModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.database.job.DistributionJobRepository;

@Component
public class DefaultProcessingJobAccessor2 implements ProcessingJobAccessor2 {

    private final DistributionJobRepository distributionJobRepository;

    @Autowired
    public DefaultProcessingJobAccessor2(DistributionJobRepository distributionJobRepository) {
        this.distributionJobRepository = distributionJobRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AlertPagedModel<SimpleFilteredDistributionJobResponseModel> getMatchingEnabledJobsForNotifications(
        FilteredDistributionJobRequestModel filteredDistributionJobRequestModel,
        int pageNumber,
        int pageLimit
    ) {
        List<String> frequencyTypes = filteredDistributionJobRequestModel.getFrequencyTypes()
            .stream()
            .map(Enum::name)
            .collect(Collectors.toList());

        Set<String> projectNames = filteredDistributionJobRequestModel.getProjectName();
        Set<String> notificationTypes = filteredDistributionJobRequestModel.getNotificationTypes();

        // If no policies and/or vulnerabilitySeverities exist the repository query expects a null to be passed
        Set<String> policyNames = filteredDistributionJobRequestModel.getPolicyNames().isEmpty() ? null : filteredDistributionJobRequestModel.getPolicyNames();
        Set<String> vulnerabilitySeverities = filteredDistributionJobRequestModel.getVulnerabilitySeverities().isEmpty() ?
            null :
            filteredDistributionJobRequestModel.getVulnerabilitySeverities();

        PageRequest pageRequest = PageRequest.of(pageNumber, pageLimit);
        Page<SimpleFilteredDistributionJobResponseModel> pageOfDistributionJobEntities = distributionJobRepository.findAndSortMatchingEnabledJobsByFilteredNotification(
            filteredDistributionJobRequestModel.getNotificationId().orElse(null),
            filteredDistributionJobRequestModel.getProviderConfigId(),
            frequencyTypes,
            projectNames,
            policyNames,
            vulnerabilitySeverities,
            pageRequest
        );

        List<SimpleFilteredDistributionJobResponseModel> distributionJobResponseModels = pageOfDistributionJobEntities.getContent();
        return new AlertPagedModel<>(pageOfDistributionJobEntities.getTotalPages(), pageNumber, pageLimit, distributionJobResponseModels);

    }
}
