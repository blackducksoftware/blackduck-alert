package com.synopsys.integration.alert.database.job.blackduck;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.database.job.blackduck.notification.BlackDuckJobNotificationTypeEntity;
import com.synopsys.integration.alert.database.job.blackduck.notification.BlackDuckJobNotificationTypeRepository;
import com.synopsys.integration.alert.database.job.blackduck.policy.BlackDuckJobPolicyFilterEntity;
import com.synopsys.integration.alert.database.job.blackduck.policy.BlackDuckJobPolicyFilterRepository;
import com.synopsys.integration.alert.database.job.blackduck.projects.BlackDuckJobProjectEntity;
import com.synopsys.integration.alert.database.job.blackduck.projects.BlackDuckJobProjectRepository;
import com.synopsys.integration.alert.database.job.blackduck.vulnerability.BlackDuckJobVulnerabilitySeverityFilterEntity;
import com.synopsys.integration.alert.database.job.blackduck.vulnerability.BlackDuckJobVulnerabilitySeverityFilterRepository;

@Component
public class BlackDuckJobDetailsAccessor {
    private final BlackDuckJobDetailsRepository blackDuckJobDetailsRepository;
    private final BlackDuckJobNotificationTypeRepository blackDuckJobNotificationTypeRepository;
    private final BlackDuckJobProjectRepository blackDuckJobProjectRepository;
    private final BlackDuckJobPolicyFilterRepository blackDuckJobPolicyFilterRepository;
    private final BlackDuckJobVulnerabilitySeverityFilterRepository blackDuckJobVulnerabilitySeverityFilterRepository;

    @Autowired
    public BlackDuckJobDetailsAccessor(
        BlackDuckJobDetailsRepository blackDuckJobDetailsRepository,
        BlackDuckJobNotificationTypeRepository blackDuckJobNotificationTypeRepository,
        BlackDuckJobProjectRepository blackDuckJobProjectRepository,
        BlackDuckJobPolicyFilterRepository blackDuckJobPolicyFilterRepository,
        BlackDuckJobVulnerabilitySeverityFilterRepository blackDuckJobVulnerabilitySeverityFilterRepository
    ) {
        this.blackDuckJobDetailsRepository = blackDuckJobDetailsRepository;
        this.blackDuckJobNotificationTypeRepository = blackDuckJobNotificationTypeRepository;
        this.blackDuckJobProjectRepository = blackDuckJobProjectRepository;
        this.blackDuckJobPolicyFilterRepository = blackDuckJobPolicyFilterRepository;
        this.blackDuckJobVulnerabilitySeverityFilterRepository = blackDuckJobVulnerabilitySeverityFilterRepository;
    }

    public BlackDuckJobDetailsEntity saveBlackDuckJobDetails(UUID jobId, DistributionJobModel distributionJobModel) {
        BlackDuckJobDetailsEntity blackDuckJobDetailsToSave = new BlackDuckJobDetailsEntity(
            jobId,
            distributionJobModel.getBlackDuckGlobalConfigId(),
            distributionJobModel.isFilterByProject(),
            distributionJobModel.getProjectNamePattern().orElse(null)
        );
        BlackDuckJobDetailsEntity savedBlackDuckJobDetails = blackDuckJobDetailsRepository.save(blackDuckJobDetailsToSave);

        List<BlackDuckJobNotificationTypeEntity> notificationTypesToSave = distributionJobModel.getNotificationTypes()
                                                                               .stream()
                                                                               .map(notificationType -> new BlackDuckJobNotificationTypeEntity(jobId, notificationType))
                                                                               .collect(Collectors.toList());
        List<BlackDuckJobNotificationTypeEntity> savedNotificationTypes = blackDuckJobNotificationTypeRepository.saveAll(notificationTypesToSave);
        savedBlackDuckJobDetails.setBlackDuckJobNotificationTypes(savedNotificationTypes);

        List<BlackDuckJobProjectEntity> ProjectFiltersToSave = distributionJobModel.getProjectFilterProjectNames()
                                                                   .stream()
                                                                   .map(projectName -> new BlackDuckJobProjectEntity(jobId, projectName))
                                                                   .collect(Collectors.toList());
        List<BlackDuckJobProjectEntity> savedProjectFilters = blackDuckJobProjectRepository.saveAll(ProjectFiltersToSave);
        savedBlackDuckJobDetails.setBlackDuckJobProjects(savedProjectFilters);

        List<BlackDuckJobPolicyFilterEntity> policyFiltersToSave = distributionJobModel.getPolicyFilterPolicyNames()
                                                                       .stream()
                                                                       .map(policyName -> new BlackDuckJobPolicyFilterEntity(jobId, policyName))
                                                                       .collect(Collectors.toList());
        List<BlackDuckJobPolicyFilterEntity> savedPolicyFilters = blackDuckJobPolicyFilterRepository.saveAll(policyFiltersToSave);
        savedBlackDuckJobDetails.setBlackDuckJobPolicyFilters(savedPolicyFilters);

        List<BlackDuckJobVulnerabilitySeverityFilterEntity> vulnerabilitySeverityFiltersToSave = distributionJobModel.getVulnerabilityFilterSeverityNames()
                                                                                                     .stream()
                                                                                                     .map(severityName -> new BlackDuckJobVulnerabilitySeverityFilterEntity(jobId, severityName))
                                                                                                     .collect(Collectors.toList());
        List<BlackDuckJobVulnerabilitySeverityFilterEntity> savedVulnerabilitySeverityFilters = blackDuckJobVulnerabilitySeverityFilterRepository.saveAll(vulnerabilitySeverityFiltersToSave);
        savedBlackDuckJobDetails.setBlackDuckJobVulnerabilitySeverityFilters(savedVulnerabilitySeverityFilters);

        return savedBlackDuckJobDetails;
    }

}
