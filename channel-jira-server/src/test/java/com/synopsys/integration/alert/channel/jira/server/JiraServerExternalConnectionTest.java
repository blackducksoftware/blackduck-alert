package com.synopsys.integration.alert.channel.jira.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.channel.jira.server.distribution.JiraServerMessageSenderFactory;
import com.synopsys.integration.alert.channel.jira.server.distribution.JiraServerProcessorFactory;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
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
public class JiraServerExternalConnectionTest {
    private final TestProperties testProperties = new TestProperties();

    //This test is @Disabled since it requires a running Jira Server instance. In order to run this test, you must deploy a Jira Server and
    // add the Jira Server environment values into test.properties
    @Test
    @Disabled
    public void sendJiraServerMessageTest() throws AlertException {
        Gson gson = new Gson();
        JiraMessageFormatter jiraMessageFormatter = new JiraMessageFormatter();

        JiraServerChannelKey jiraServerChannelKey = new JiraServerChannelKey();
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(), Mockito.any())).thenReturn(List.of(createConfigurationModelForJiraServer()));
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfoForHost(Mockito.anyString())).thenReturn(null);
        JiraServerPropertiesFactory jiraServerPropertiesFactory = new JiraServerPropertiesFactory(jiraServerChannelKey, proxyManager, configurationModelConfigurationAccessor);

        IssueTrackerCallbackInfoCreator issueTrackerCallbackInfoCreator = new IssueTrackerCallbackInfoCreator();
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();
        JiraServerMessageSenderFactory jiraServerMessageSenderFactory = new JiraServerMessageSenderFactory(gson, jiraServerChannelKey, jiraServerPropertiesFactory, issueTrackerCallbackInfoCreator,
            issueCategoryRetriever);

        ProjectMessageToIssueModelTransformer modelTransformer = new ProjectMessageToIssueModelTransformer();
        JiraServerProcessorFactory jiraServerProcessorFactory = new JiraServerProcessorFactory(gson, jiraMessageFormatter, jiraServerPropertiesFactory, jiraServerMessageSenderFactory, modelTransformer, issueCategoryRetriever);
        IssueTrackerProcessor<String> processor = jiraServerProcessorFactory.createProcessor(createDistributionDetails());

        IssueTrackerResponse<String> response = processor.processMessages(createMessage(), "jobName");

        assertEquals("Success", response.getStatusMessage());
    }

    private ProviderMessageHolder createMessage() {
        ProviderDetails providerDetails = new ProviderDetails(1L, new LinkableItem("ProviderLabel", "ProviderName"));
        SimpleMessage simpleMessage = SimpleMessage.original(providerDetails, "Test Summary", "Test Description", List.of());

        return new ProviderMessageHolder(List.of(), List.of(simpleMessage));
    }

    private ConfigurationModel createConfigurationModelForJiraServer() {
        Map<String, ConfigurationFieldModel> configuredFields = new HashMap<>();
        addConfigurationFieldToMap(configuredFields, JiraServerDescriptor.KEY_SERVER_URL, testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_URL));
        addConfigurationFieldToMap(configuredFields, JiraServerDescriptor.KEY_SERVER_USERNAME, testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_USERNAME));
        addConfigurationFieldToMap(configuredFields, JiraServerDescriptor.KEY_SERVER_PASSWORD, testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_PASSWORD));
        addConfigurationFieldToMap(configuredFields, JiraServerDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK, testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_URL).orElse("false"));

        return new ConfigurationModel(null, null, null, null, null, configuredFields);
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

    private void addConfigurationFieldToMap(Map<String, ConfigurationFieldModel> configuredFields, String key, String value) {
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(key);
        configurationFieldModel.setFieldValue(value);
        configuredFields.put(key, configurationFieldModel);
    }

}
