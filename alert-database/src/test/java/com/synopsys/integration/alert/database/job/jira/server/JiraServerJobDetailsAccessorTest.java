package com.synopsys.integration.alert.database.job.jira.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.alert.database.job.jira.server.custom_field.JiraServerJobCustomFieldEntity;
import com.synopsys.integration.alert.database.job.jira.server.custom_field.JiraServerJobCustomFieldRepository;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

class JiraServerJobDetailsAccessorTest {
    private final JiraServerChannelKey channelKey = ChannelKeys.JIRA_SERVER;
    private JiraServerJobDetailsRepository jobDetailsRepository;
    private JiraServerJobCustomFieldRepository jiraServerJobCustomFieldRepository;
    private DefaultJiraServerJobDetailsAccessor jobDetailsAccessor;

    @BeforeEach
    public void init() {
        jobDetailsRepository = Mockito.mock(JiraServerJobDetailsRepository.class);
        jiraServerJobCustomFieldRepository = Mockito.mock(JiraServerJobCustomFieldRepository.class);

        jobDetailsAccessor = new DefaultJiraServerJobDetailsAccessor(channelKey, jobDetailsRepository, jiraServerJobCustomFieldRepository);
    }

    @Test
    void getChannelKeyTest() {
        assertEquals(channelKey, jobDetailsAccessor.getDescriptorKey());
    }

    @Test
    void saveJobDetailsTest() {
        UUID jobId = UUID.randomUUID();

        List<JiraServerJobCustomFieldEntity> customFields = createCustomFieldEntities(jobId);
        JiraServerJobDetailsEntity jiraJobDetailsEntity = createDetailsEntity(jobId);
        jiraJobDetailsEntity.setJobCustomFields(customFields);
        JiraServerJobDetailsModel jiraJobDetailsModel = createDetailsModel(jiraJobDetailsEntity);
        Mockito.when(jiraServerJobCustomFieldRepository.saveAll(Mockito.any())).thenReturn(customFields);
        Mockito.when(jiraServerJobCustomFieldRepository.findByJobId(jobId)).thenReturn(customFields);
        Mockito.when(jobDetailsRepository.findById(jobId)).thenReturn(Optional.of(jiraJobDetailsEntity));
        Mockito.when(jobDetailsRepository.save(Mockito.any())).thenReturn(jiraJobDetailsEntity);
        JiraServerJobDetailsModel newJiraJobDetails = jobDetailsAccessor.saveJobDetails(jobId, jiraJobDetailsModel);

        assertEquals(jobId, newJiraJobDetails.getJobId());
        assertNotNull(newJiraJobDetails.getCustomFields());
        assertEquals(1, newJiraJobDetails.getCustomFields().size());
    }

    @Test
    void retrieveDetailsTest() {
        UUID jobId = UUID.randomUUID();

        List<JiraServerJobCustomFieldEntity> customFields = createCustomFieldEntities(jobId);
        JiraServerJobDetailsEntity jiraJobDetailsEntity = createDetailsEntity(jobId);
        jiraJobDetailsEntity.setJobCustomFields(customFields);

        Mockito.when(jobDetailsRepository.findById(jobId)).thenReturn(Optional.of(jiraJobDetailsEntity));
        Mockito.when(jiraServerJobCustomFieldRepository.findByJobId(jobId)).thenReturn(customFields);
        JiraServerJobDetailsModel foundJobDetailsModel = jobDetailsAccessor.retrieveDetails(jobId).orElse(null);
        assertNotNull(foundJobDetailsModel);
        assertEquals(jobId, foundJobDetailsModel.getJobId());
    }

    @Test
    void retrieveDetailsUnknownIdTest() {
        UUID jobId = UUID.randomUUID();

        Mockito.when(jobDetailsRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Optional<JiraServerJobDetailsModel> foundJobDetailsModel = jobDetailsAccessor.retrieveDetails(jobId);
        assertTrue(foundJobDetailsModel.isEmpty());
    }

    private JiraServerJobDetailsModel createDetailsModel(JiraServerJobDetailsEntity jobDetailsModel) {
        return new JiraServerJobDetailsModel(jobDetailsModel.getJobId(), jobDetailsModel.getAddComments(), jobDetailsModel.getIssueCreatorUsername(), jobDetailsModel.getProjectNameOrKey(),
            jobDetailsModel.getIssueType(), jobDetailsModel.getResolveTransition(), jobDetailsModel.getReopenTransition(), createCustomFieldModels(), jobDetailsModel.getIssueSummary());
    }

    private JiraServerJobDetailsEntity createDetailsEntity(UUID jobId) {
        return new JiraServerJobDetailsEntity(jobId, false, "user", "project",
            "Task", "Resolve", "Reopen", "summary");
    }

    private List<JiraJobCustomFieldModel> createCustomFieldModels() {
        return List.of(new JiraJobCustomFieldModel("customField", "customFieldValue"));
    }

    private List<JiraServerJobCustomFieldEntity> createCustomFieldEntities(UUID jobID) {
        return List.of(new JiraServerJobCustomFieldEntity(jobID, "customField", "customFieldValue"));
    }
}
