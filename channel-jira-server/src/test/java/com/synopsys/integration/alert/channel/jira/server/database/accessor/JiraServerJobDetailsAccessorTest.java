package com.synopsys.integration.alert.channel.jira.server.database.accessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.alert.database.job.jira.server.DefaultJiraServerJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.jira.server.JiraServerJobDetailsEntity;
import com.synopsys.integration.alert.database.job.jira.server.JiraServerJobDetailsRepository;
import com.synopsys.integration.alert.database.job.jira.server.custom_field.JiraServerJobCustomFieldRepository;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

class JiraServerJobDetailsAccessorTest {
    private final JiraServerChannelKey channelKey = ChannelKeys.JIRA_SERVER;
    private JiraServerJobDetailsRepository jobDetailsRepository;
    private DefaultJiraServerJobDetailsAccessor jobDetailsAccessor;

    @BeforeEach
    public void init() {
        jobDetailsRepository = Mockito.mock(JiraServerJobDetailsRepository.class);
        JiraServerJobCustomFieldRepository jiraServerJobCustomFieldRepository = Mockito.mock(JiraServerJobCustomFieldRepository.class);

        jobDetailsAccessor = new DefaultJiraServerJobDetailsAccessor(channelKey, jobDetailsRepository, jiraServerJobCustomFieldRepository);
    }

    @Test
    void getChannelKeyTest() {
        assertEquals(channelKey, jobDetailsAccessor.getDescriptorKey());
    }

    @Test
    void saveEmailJobDetailsTest() {
        UUID jobId = UUID.randomUUID();

        JiraServerJobDetailsEntity jiraJobDetailsEntity = createDetailsEntity(jobId);
        JiraServerJobDetailsModel jiraJobDetailsModel = createDetailsModel(jiraJobDetailsEntity);

        Mockito.when(jobDetailsRepository.save(Mockito.any())).thenReturn(jiraJobDetailsEntity);
        JiraServerJobDetailsModel newEmailJobDetails = jobDetailsAccessor.saveJobDetails(jobId, jiraJobDetailsModel);

        assertEquals(jobId, newEmailJobDetails.getJobId());
    }

    @Test
    void retrieveDetailsTest() {
        UUID jobId = UUID.randomUUID();

        JiraServerJobDetailsEntity jiraJobDetailsEntity = createDetailsEntity(jobId);

        Mockito.when(jobDetailsRepository.findById(Mockito.any())).thenReturn(Optional.of(jiraJobDetailsEntity));
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
            jobDetailsModel.getIssueType(), jobDetailsModel.getResolveTransition(), jobDetailsModel.getReopenTransition(), List.of(), jobDetailsModel.getIssueSummary());
    }

    private JiraServerJobDetailsEntity createDetailsEntity(UUID jobId) {
        return new JiraServerJobDetailsEntity(jobId, false, "user", "project",
            "Task", "Resolve", "Reopen", "summary");
    }
}
