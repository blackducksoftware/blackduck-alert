package com.synopsys.integration.alert.channel.jira.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.IssueTrackerProcessor;
import com.synopsys.integration.alert.api.channel.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.api.channel.issue.convert.ProjectMessageToIssueModelTransformer;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerResponse;
import com.synopsys.integration.alert.api.channel.issue.search.IssueCategoryRetriever;
import com.synopsys.integration.alert.api.channel.jira.distribution.JiraMessageFormatter;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.jira.server.distribution.JiraServerMessageSenderFactory;
import com.synopsys.integration.alert.channel.jira.server.distribution.JiraServerProcessorFactory;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobSubTaskAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModelBuilder;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestTags;

@Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
class JiraServerExternalConnectionTest {
    private final TestProperties testProperties = new TestProperties();

    //This test is @Disabled since it requires a running Jira Server instance. In order to run this test, you must deploy a Jira Server and
    // add the Jira Server environment values into test.properties
    @Test
    @Disabled
    void sendJiraServerMessageTest() throws AlertException {
        Gson gson = new Gson();
        JiraMessageFormatter jiraMessageFormatter = new JiraMessageFormatter();

        JiraServerChannelKey jiraServerChannelKey = new JiraServerChannelKey();
        EventManager eventManager = Mockito.mock(EventManager.class);
        ExecutingJobManager executingJobManager = Mockito.mock(ExecutingJobManager.class);
        JobSubTaskAccessor jobSubTaskAccessor = Mockito.mock(JobSubTaskAccessor.class);
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(Mockito.anyString())).thenReturn(Optional.of(createJiraServerConfigModel()));
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfoForHost(Mockito.anyString())).thenReturn(null);
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(createDistributionJobModel()));
        JiraServerPropertiesFactory jiraServerPropertiesFactory = new JiraServerPropertiesFactory(proxyManager, jiraServerGlobalConfigAccessor, jobAccessor);

        IssueTrackerCallbackInfoCreator issueTrackerCallbackInfoCreator = new IssueTrackerCallbackInfoCreator();
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();
        JiraServerMessageSenderFactory jiraServerMessageSenderFactory = new JiraServerMessageSenderFactory(
            gson,
            jiraServerChannelKey,
            jiraServerPropertiesFactory,
            issueTrackerCallbackInfoCreator,
            issueCategoryRetriever,
            eventManager,
            jobSubTaskAccessor,
            executingJobManager
        );

        ProjectMessageToIssueModelTransformer modelTransformer = new ProjectMessageToIssueModelTransformer();
        JiraServerProcessorFactory jiraServerProcessorFactory = new JiraServerProcessorFactory(
            gson,
            jiraMessageFormatter,
            jiraServerPropertiesFactory,
            jiraServerMessageSenderFactory,
            modelTransformer,
            issueCategoryRetriever
        );
        IssueTrackerProcessor<String> processor = jiraServerProcessorFactory.createProcessor(createDistributionDetails(), UUID.randomUUID(), UUID.randomUUID(), Set.of());

        IssueTrackerResponse<String> response = processor.processMessages(createMessage(), "jobName");

        assertEquals("Success", response.getStatusMessage());
    }

    private ProviderMessageHolder createMessage() {
        ProviderDetails providerDetails = new ProviderDetails(1L, new LinkableItem("ProviderLabel", "ProviderName"));
        SimpleMessage simpleMessage = SimpleMessage.original(providerDetails, "Test Summary", "Test Description", List.of());

        return new ProviderMessageHolder(List.of(), List.of(simpleMessage));
    }

    private JiraServerGlobalConfigModel createJiraServerConfigModel() {
        return new JiraServerGlobalConfigModel(
            UUID.randomUUID().toString(),
            "name",
            testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_URL),
            testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_USERNAME),
            testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_PASSWORD)
        );
    }

    private DistributionJobModel createDistributionJobModel() {
        return new DistributionJobModelBuilder()
            .channelGlobalConfigId(UUID.randomUUID())
            .name("name")
            .distributionFrequency(FrequencyType.REAL_TIME)
            .processingType(ProcessingType.DEFAULT)
            .channelDescriptorName("channelName")
            .createdAt(OffsetDateTime.now())
            .blackDuckGlobalConfigId(1L)
            .notificationTypes(List.of("POLICY_OVERRIDE"))
            .build();
    }

    private JiraServerJobDetailsModel createDistributionDetails() {
        UUID uuid = UUID.randomUUID();
        List<JiraJobCustomFieldModel> customFields = new ArrayList<>();
        //This test requires that the JIRA server has 2 components associated with the project: "component1" and "component2"
        customFields.add(new JiraJobCustomFieldModel("Component/s", "component1 component2"));

        return new JiraServerJobDetailsModel(uuid,
            Boolean.parseBoolean(testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_ADD_COMMENTS)),
            testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_ISSUE_CREATOR).orElse(null),
            testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_PROJECT_NAME),
            testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_ISSUE_TYPE),
            testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_RESOLVE_TRANSITION),
            testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_REOPEN_TRANSITION),
            customFields,
            ""
        );
    }

}
