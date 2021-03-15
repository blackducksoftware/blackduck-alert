package com.synopsys.integration.alert.channel.slack;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.channel.ChannelDescriptorTestIT;
import com.synopsys.integration.alert.channel.slack.action.SlackDistributionTestAction;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.channel.slack.distribution.SlackChannel;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.channel.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.test.common.TestPropertyKey;

public class SlackChannelChannelDescriptorTestIT extends ChannelDescriptorTestIT {
    @Autowired
    private SlackDescriptor slackDescriptor;
    @Autowired
    private SlackChannel slackChannel;

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
