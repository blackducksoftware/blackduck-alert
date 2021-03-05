package com.synopsys.integration.alert.channel.slack;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.channel.ChannelDescriptorTestIT;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.channel.slack2.SlackChannelV2;
import com.synopsys.integration.alert.channel.slack2.action.SlackDistributionTestAction;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.channel.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.rest.RestConstants;

public class SlackChannelChannelDescriptorTestIT extends ChannelDescriptorTestIT {
    @Autowired
    private SlackDescriptor slackDescriptor;
    @Autowired
    private SlackChannelV2 slackChannel;

    @BeforeEach
    public void testSetup() {
        String blackDuckTimeoutKey = BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT;
        ConfigurationFieldModel blackDuckTimeoutField = ConfigurationFieldModel.create(blackDuckTimeoutKey);
        blackDuckTimeoutField.setFieldValue(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TIMEOUT));

        String blackDuckApiKey = BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY;
        ConfigurationFieldModel blackDuckApiField = ConfigurationFieldModel.create(blackDuckApiKey);
        blackDuckApiField.setFieldValue(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_API_KEY));

        String blackDuckProviderUrlKey = BlackDuckDescriptor.KEY_BLACKDUCK_URL;
        ConfigurationFieldModel blackDuckProviderUrlField = ConfigurationFieldModel.create(blackDuckProviderUrlKey);
        blackDuckProviderUrlField.setFieldValue(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL));

        providerGlobalConfig = configurationAccessor
                                   .createConfiguration(providerKey, ConfigContextEnum.GLOBAL, List.of(blackDuckTimeoutField, blackDuckApiField, blackDuckProviderUrlField));
    }

    @Override
    public Optional<ConfigurationModel> saveGlobalConfiguration() {
        return Optional.empty();
    }

    @Override
    public DistributionJobDetailsModel createDistributionJobDetails() {
        return new SlackJobDetailsModel(
            null,
            testProperties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK),
            testProperties.getProperty(TestPropertyKey.TEST_SLACK_CHANNEL_NAME),
            getClass().getSimpleName()
        );
    }

    @Override
    public DistributionEvent createChannelEvent() throws AlertException {
        LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        ProviderMessageContent content = new ProviderMessageContent.Builder()
                                             .applyProvider("testProvider", 1L, "testProviderConfig")
                                             .applyTopic("testTopic", "")
                                             .applySubTopic(subTopic.getLabel(), subTopic.getValue())
                                             .build();

        String createdAt = DateUtils.formatDate(DateUtils.createCurrentDateTimestamp(), RestConstants.JSON_DATE_FORMAT);
        DistributionEvent event = new DistributionEvent(
            ChannelKeys.SLACK.getUniversalKey(),
            createdAt,
            1L,
            ProcessingType.DEFAULT.name(),
            MessageContentGroup.singleton(content),
            distributionJobModel,
            null
        );
        return event;
    }

    @Override
    public ChannelDescriptor getDescriptor() {
        return slackDescriptor;
    }

    @Override
    public boolean assertGlobalFields(Set<DefinedFieldModel> globalFields) {
        return globalFields.isEmpty(); // no global fields for slack
    }

    @Override
    public boolean assertDistributionFields(Set<DefinedFieldModel> distributionFields) {
        Set<String> fieldNames = Set.of(SlackDescriptor.KEY_CHANNEL_NAME, SlackDescriptor.KEY_CHANNEL_USERNAME, SlackDescriptor.KEY_WEBHOOK);
        Set<String> passedFieldNames = distributionFields.stream().map(DefinedFieldModel::getKey).collect(Collectors.toSet());
        return passedFieldNames.containsAll(fieldNames);
    }

    @Override
    public Map<String, String> createInvalidGlobalFieldMap() {
        return Map.of();
    }

    @Override
    public FieldModel createTestConfigDestination() {
        return createFieldModel(ChannelKeys.SLACK.getUniversalKey(), "");
    }

    @Override
    public String getEventDestinationName() {
        return ChannelKeys.SLACK.getUniversalKey();
    }

    @Override
    public ChannelDistributionTestAction getChannelDistributionTestAction() {
        return new SlackDistributionTestAction(slackChannel) {};
    }

    @Override
    public void testGlobalConfig() {
        // Slack has no global config
    }

    @Override
    public void testGlobalValidate() {
        // Slack has no global config
    }

    @Override
    public void testGlobalValidateWithFieldErrors() {
        // Slack has no global config
    }

    @Override
    public TestAction getGlobalTestAction() {
        // Slack has no global config
        return null;
    }

}
