package com.synopsys.integration.alert.database.job.blackduck;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.database.job.blackduck.notification.BlackDuckJobNotificationTypeEntity;
import com.synopsys.integration.alert.database.job.blackduck.notification.BlackDuckJobNotificationTypeRepository;
import com.synopsys.integration.alert.database.job.blackduck.policy.BlackDuckJobPolicyFilterEntity;
import com.synopsys.integration.alert.database.job.blackduck.policy.BlackDuckJobPolicyFilterRepository;
import com.synopsys.integration.alert.database.job.blackduck.projects.BlackDuckJobProjectEntity;
import com.synopsys.integration.alert.database.job.blackduck.projects.BlackDuckJobProjectRepository;
import com.synopsys.integration.alert.database.job.blackduck.vulnerability.BlackDuckJobVulnerabilitySeverityFilterEntity;
import com.synopsys.integration.alert.database.job.blackduck.vulnerability.BlackDuckJobVulnerabilitySeverityFilterRepository;

public class BlackDuckJobDetailsAccessorTest {
    private BlackDuckJobDetailsRepository blackDuckJobDetailsRepository;
    private BlackDuckJobNotificationTypeRepository blackDuckJobNotificationTypeRepository;
    private BlackDuckJobProjectRepository blackDuckJobProjectRepository;
    private BlackDuckJobPolicyFilterRepository blackDuckJobPolicyFilterRepository;
    private BlackDuckJobVulnerabilitySeverityFilterRepository blackDuckJobVulnerabilitySeverityFilterRepository;

    private BlackDuckJobDetailsAccessor blackDuckJobDetailsAccessor;

    private final String jobName = "jobName";

    @BeforeEach
    public void init() {
        blackDuckJobDetailsRepository = Mockito.mock(BlackDuckJobDetailsRepository.class);
        blackDuckJobNotificationTypeRepository = Mockito.mock(BlackDuckJobNotificationTypeRepository.class);
        blackDuckJobProjectRepository = Mockito.mock(BlackDuckJobProjectRepository.class);
        blackDuckJobPolicyFilterRepository = Mockito.mock(BlackDuckJobPolicyFilterRepository.class);
        blackDuckJobVulnerabilitySeverityFilterRepository = Mockito.mock(BlackDuckJobVulnerabilitySeverityFilterRepository.class);

        blackDuckJobDetailsAccessor = new BlackDuckJobDetailsAccessor(blackDuckJobDetailsRepository,
            blackDuckJobNotificationTypeRepository,
            blackDuckJobProjectRepository,
            blackDuckJobPolicyFilterRepository,
            blackDuckJobVulnerabilitySeverityFilterRepository);
    }

    @Test
    public void saveBlackDuckJobDetailsTest() {
        UUID jobId = UUID.randomUUID();
        DistributionJobRequestModel distributionJobRequestModel = new DistributionJobRequestModel(
            true,
            jobName,
            FrequencyType.DAILY,
            ProcessingType.DEFAULT,
            null,
            3L,
            true,
            "*",
            "*",
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            null
        );

        BlackDuckJobDetailsEntity blackDuckJobDetailsEntity = new BlackDuckJobDetailsEntity(
            jobId,
            distributionJobRequestModel.getBlackDuckGlobalConfigId(),
            distributionJobRequestModel.isFilterByProject(),
            distributionJobRequestModel.getProjectNamePattern().orElse(null),
            distributionJobRequestModel.getProjectVersionNamePattern().orElse(null)
        );

        BlackDuckJobNotificationTypeEntity blackDuckJobNotificationTypeEntity = new BlackDuckJobNotificationTypeEntity(jobId, "testNotificationType");
        BlackDuckJobProjectEntity blackDuckJobProjectEntity = new BlackDuckJobProjectEntity();
        BlackDuckJobPolicyFilterEntity blackDuckJobPolicyFilterEntity = new BlackDuckJobPolicyFilterEntity();
        BlackDuckJobVulnerabilitySeverityFilterEntity blackDuckJobVulnerabilitySeverityFilterEntity = new BlackDuckJobVulnerabilitySeverityFilterEntity();

        Mockito.when(blackDuckJobDetailsRepository.save(Mockito.any())).thenReturn(blackDuckJobDetailsEntity);
        Mockito.when(blackDuckJobNotificationTypeRepository.saveAll(Mockito.any())).thenReturn(List.of(blackDuckJobNotificationTypeEntity));
        Mockito.when(blackDuckJobProjectRepository.saveAll(Mockito.any())).thenReturn(List.of(blackDuckJobProjectEntity));
        Mockito.when(blackDuckJobPolicyFilterRepository.saveAll(Mockito.any())).thenReturn(List.of(blackDuckJobPolicyFilterEntity));
        Mockito.when(blackDuckJobVulnerabilitySeverityFilterRepository.saveAll(Mockito.any())).thenReturn(List.of(blackDuckJobVulnerabilitySeverityFilterEntity));

        BlackDuckJobDetailsEntity createdBlackDuckJobDetailsEntity = blackDuckJobDetailsAccessor.saveBlackDuckJobDetails(jobId, distributionJobRequestModel);

        Mockito.verify(blackDuckJobNotificationTypeRepository).bulkDeleteAllByJobId(Mockito.eq(jobId));
        Mockito.verify(blackDuckJobProjectRepository).bulkDeleteAllByJobId(Mockito.eq(jobId));
        Mockito.verify(blackDuckJobPolicyFilterRepository).bulkDeleteAllByJobId(Mockito.eq(jobId));
        Mockito.verify(blackDuckJobVulnerabilitySeverityFilterRepository).bulkDeleteAllByJobId(Mockito.eq(jobId));

        assertEquals(jobId, createdBlackDuckJobDetailsEntity.getJobId());
        assertEquals(1, createdBlackDuckJobDetailsEntity.getBlackDuckJobNotificationTypes().size());
        assertEquals(blackDuckJobNotificationTypeEntity, createdBlackDuckJobDetailsEntity.getBlackDuckJobNotificationTypes().get(0));
        assertEquals(1, createdBlackDuckJobDetailsEntity.getBlackDuckJobProjects().size());
        assertEquals(blackDuckJobProjectEntity, createdBlackDuckJobDetailsEntity.getBlackDuckJobProjects().get(0));
        assertEquals(1, createdBlackDuckJobDetailsEntity.getBlackDuckJobPolicyFilters().size());
        assertEquals(blackDuckJobPolicyFilterEntity, createdBlackDuckJobDetailsEntity.getBlackDuckJobPolicyFilters().get(0));
        assertEquals(1, createdBlackDuckJobDetailsEntity.getBlackDuckJobVulnerabilitySeverityFilters().size());
        assertEquals(blackDuckJobVulnerabilitySeverityFilterEntity, createdBlackDuckJobDetailsEntity.getBlackDuckJobVulnerabilitySeverityFilters().get(0));
    }

    @Test
    public void retrieveNotificationTypesForJobTest() {
        UUID jobId = UUID.randomUUID();
        String notificationType = "testNotificationType";
        BlackDuckJobNotificationTypeEntity blackDuckJobNotificationTypeEntity = new BlackDuckJobNotificationTypeEntity(jobId, notificationType);

        Mockito.when(blackDuckJobNotificationTypeRepository.findByJobId(Mockito.eq(jobId))).thenReturn(List.of(blackDuckJobNotificationTypeEntity));

        List<String> notificationTypes = blackDuckJobDetailsAccessor.retrieveNotificationTypesForJob(jobId);

        assertEquals(1, notificationTypes.size());
        assertEquals(notificationType, notificationTypes.get(0));
    }

    @Test
    public void retrieveProjectDetailsForJobTest() {
        UUID jobId = UUID.randomUUID();
        String projectName = "projectName";
        String href = "href";
        BlackDuckJobProjectEntity blackDuckJobProjectEntity = new BlackDuckJobProjectEntity(jobId, projectName, href);

        Mockito.when(blackDuckJobProjectRepository.findByJobId(Mockito.eq(jobId))).thenReturn(List.of(blackDuckJobProjectEntity));

        List<BlackDuckProjectDetailsModel> blackDuckProjectDetailsModels = blackDuckJobDetailsAccessor.retrieveProjectDetailsForJob(jobId);

        assertEquals(1, blackDuckProjectDetailsModels.size());
        BlackDuckProjectDetailsModel blackDuckProjectDetailsModel = blackDuckProjectDetailsModels.get(0);
        assertEquals(projectName, blackDuckProjectDetailsModel.getName());
        assertEquals(href, blackDuckProjectDetailsModel.getHref());
    }

    @Test
    public void retrievePolicyNamesForJobTest() {
        UUID jobId = UUID.randomUUID();
        String policyName = "policyName";
        BlackDuckJobPolicyFilterEntity blackDuckJobPolicyFilterEntity = new BlackDuckJobPolicyFilterEntity(jobId, policyName);

        Mockito.when(blackDuckJobPolicyFilterRepository.findByJobId(Mockito.eq(jobId))).thenReturn(List.of(blackDuckJobPolicyFilterEntity));

        List<String> policyNamesList = blackDuckJobDetailsAccessor.retrievePolicyNamesForJob(jobId);

        assertEquals(1, policyNamesList.size());
        assertEquals(policyName, policyNamesList.get(0));
    }

    @Test
    public void retrieveVulnerabilitySeverityNamesForJobTest() {
        UUID jobId = UUID.randomUUID();
        String severityName = "severityName";
        BlackDuckJobVulnerabilitySeverityFilterEntity blackDuckJobVulnerabilitySeverityFilterEntity = new BlackDuckJobVulnerabilitySeverityFilterEntity(jobId, severityName);

        Mockito.when(blackDuckJobVulnerabilitySeverityFilterRepository.findByJobId(Mockito.eq(jobId))).thenReturn(List.of(blackDuckJobVulnerabilitySeverityFilterEntity));

        List<String> vulnerabilitySeverityNames = blackDuckJobDetailsAccessor.retrieveVulnerabilitySeverityNamesForJob(jobId);

        assertEquals(1, vulnerabilitySeverityNames.size());
        assertEquals(severityName, vulnerabilitySeverityNames.get(0));
    }
}
