package com.synopsys.integration.alert.channel.jira.cloud;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.alert.api.channel.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerModelHolder;
import com.synopsys.integration.alert.api.channel.issue.search.IssueCategoryRetriever;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.jira.cloud.distribution.JiraCloudMessageSenderFactory;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Tags(value = {
    @Tag(TestTags.DEFAULT_INTEGRATION),
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
})
public class JiraCloudSummaryFieldLengthTestIT {
    private static final LinkableItem PROVIDER = new LinkableItem("Black Duck", "a hub server", "https://a-hub-server");
    private static final String TEST_NAME = JiraCloudSummaryFieldLengthTestIT.class.getSimpleName();

    private static IssueTrackerMessageSender<String> jiraCloudMessageSender;

    @BeforeAll
    public static void init() throws AlertException {
        jiraCloudMessageSender = createJiraCloudMessageSender();
    }

    @Test
    public void summaryLength254SucceedsTest() {
        IssueCreationModel issueCreationModel = createIssueCreationModel(254);
        IssueTrackerModelHolder<String> messages = new IssueTrackerModelHolder<>(List.of(issueCreationModel), List.of(), List.of());
        try {
            jiraCloudMessageSender.sendMessages(messages);
        } catch (AlertException e) {
            fail("Failed to send a message with a 254 character summary", e);
        }
    }

    @Test
    public void summaryLength256FailsTest() {
        IssueCreationModel issueCreationModel = createIssueCreationModel(256);
        IssueTrackerModelHolder<String> messages = new IssueTrackerModelHolder<>(List.of(issueCreationModel), List.of(), List.of());
        try {
            jiraCloudMessageSender.sendMessages(messages);
            fail("Successfully sent a message with a 256 character summary which is greater than the expected maximum");
        } catch (AlertException e) {
            // Pass
            e.printStackTrace();
        }
    }

    private IssueCreationModel createIssueCreationModel(int summaryLength) {
        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < summaryLength; i++) {
            if (i % 2 == 0) {
                summary.append("0");
            } else {
                summary.append("1");
            }
        }
        return IssueCreationModel.simple(summary.toString(), TEST_NAME, List.of(), PROVIDER);
    }

    private static IssueTrackerMessageSender<String> createJiraCloudMessageSender() throws AlertException {
        TestProperties testProperties = new TestProperties();
        Gson gson = new GsonBuilder().create();
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();
        JiraCloudMessageSenderFactory jiraCloudMessageSenderFactory = new JiraCloudMessageSenderFactory(
            gson,
            ChannelKeys.JIRA_CLOUD,
            createJiraCloudPropertiesFactory(testProperties),
            new IssueTrackerCallbackInfoCreator(),
            issueCategoryRetriever);
        JiraCloudJobDetailsModel jiraCloudJobDetails = createJiraCloudJobDetails(testProperties);
        return jiraCloudMessageSenderFactory.createMessageSender(jiraCloudJobDetails);
    }

    private static JiraCloudPropertiesFactory createJiraCloudPropertiesFactory(TestProperties testProperties) throws AlertConfigurationException {
        String url = testProperties.getProperty(TestPropertyKey.TEST_JIRA_CLOUD_URL);
        String apiToken = testProperties.getProperty(TestPropertyKey.TEST_JIRA_CLOUD_API_TOKEN);
        String userEmail = testProperties.getProperty(TestPropertyKey.TEST_JIRA_CLOUD_USER_EMAIL);

        JiraCloudPropertiesFactory jiraCloudPropertiesFactory = Mockito.mock(JiraCloudPropertiesFactory.class);
        JiraCloudProperties jiraCloudProperties = new JiraCloudProperties(url, apiToken, userEmail, true, ProxyInfo.NO_PROXY_INFO);
        Mockito.when(jiraCloudPropertiesFactory.createJiraProperties()).thenReturn(jiraCloudProperties);

        return jiraCloudPropertiesFactory;
    }

    private static JiraCloudJobDetailsModel createJiraCloudJobDetails(TestProperties testProperties) {
        String issueCreator = testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_CLOUD_ISSUE_CREATOR).orElse(null);
        String projectName = testProperties.getProperty(TestPropertyKey.TEST_JIRA_CLOUD_PROJECT_NAME);
        String issueType = testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_CLOUD_ISSUE_TYPE).orElse("Task");
        String resolveTransition = testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_CLOUD_RESOLVE_TRANSITION).orElse(null);
        String reopenTransition = testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_CLOUD_REOPEN_TRANSITION).orElse(null);

        return new JiraCloudJobDetailsModel(
            UUID.randomUUID(),
            true,
            issueCreator,
            projectName,
            issueType,
            resolveTransition,
            reopenTransition,
            List.of(),
            null);
    }

}
