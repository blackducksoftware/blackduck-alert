package com.synopsys.integration.alert.channel.jira.server.database.accessor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.database.job.jira.server.DefaultJiraServerJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.jira.server.JiraServerJobDetailsRepository;
import com.synopsys.integration.alert.database.job.jira.server.custom_field.JiraServerJobCustomFieldRepository;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

class JiraServerJobDetailsAccessorTest {
    private JiraServerChannelKey channelKey = ChannelKeys.JIRA_SERVER;
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
}
