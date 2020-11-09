package com.synopsys.integration.alert.channel.slack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.channel.ChannelDescriptorTestIT;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.channel.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.descriptor.api.SlackChannelKey;
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.rest.RestConstants;

public class SlackChannelChannelDescriptorTestIT extends ChannelDescriptorTestIT {
    public static final String UNIT_TEST_JOB_NAME = "SlackChatUnitTestJob";
    private static final BlackDuckProviderKey BLACK_DUCK_PROVIDER_KEY = new BlackDuckProviderKey();

    @Autowired
    private SlackDescriptor slackDescriptor;
    @Autowired
    private SlackChannel slackChannel;
    @Autowired
    private SlackChannelKey slackChannelKey;

    @BeforeEach
    public void testSetup() throws Exception {
        String blackDuckTimeoutKey = BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT;
        ConfigurationFieldModel blackDuckTimeoutField = ConfigurationFieldModel.create(blackDuckTimeoutKey);
        blackDuckTimeoutField.setFieldValue(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TIMEOUT));

        String blackDuckApiKey = BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY;
        ConfigurationFieldModel blackDuckApiField = ConfigurationFieldModel.create(blackDuckApiKey);
        blackDuckApiField.setFieldValue(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_API_KEY));

        String blackDuckProviderUrlKey = BlackDuckDescriptor.KEY_BLACKDUCK_URL;
        ConfigurationFieldModel blackDuckProviderUrlField = ConfigurationFieldModel.create(blackDuckProviderUrlKey);
        blackDuckProviderUrlField.setFieldValue(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL));

        provider_global = configurationAccessor
                              .createConfiguration(BLACK_DUCK_PROVIDER_KEY, ConfigContextEnum.GLOBAL, List.of(blackDuckTimeoutField, blackDuckApiField, blackDuckProviderUrlField));
    }

    @Override
    public Optional<ConfigurationModel> saveGlobalConfiguration() {
        return Optional.empty();
    }

    @Override
    public ConfigurationModel saveDistributionConfiguration() throws Exception {
        List<ConfigurationFieldModel> models = MockConfigurationModelFactory.createSlackDistributionFields();
        Map<String, ConfigurationFieldModel> fieldMap = MockConfigurationModelFactory.mapFieldKeyToFields(models);

        fieldMap.get(SlackDescriptor.KEY_WEBHOOK).setFieldValue(properties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK));
        fieldMap.get(SlackDescriptor.KEY_CHANNEL_NAME).setFieldValue(properties.getProperty(TestPropertyKey.TEST_SLACK_CHANNEL_NAME));
        fieldMap.get(SlackDescriptor.KEY_CHANNEL_USERNAME).setFieldValue(properties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME));
        return configurationAccessor.createConfiguration(slackChannelKey, ConfigContextEnum.DISTRIBUTION, models);
    }

    @Override
    public DistributionEvent createChannelEvent() throws AlertException {
        LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        ProviderMessageContent content = new ProviderMessageContent.Builder()
                                             .applyProvider("testProvider", 1L, "testProviderConfig")
                                             .applyTopic("testTopic", "")
                                             .applySubTopic(subTopic.getName(), subTopic.getValue())
                                             .build();
        List<ConfigurationModel> models = List.of();
        try {
            models = configurationAccessor.getConfigurationsByDescriptorKey(slackChannelKey);
        } catch (AlertDatabaseConstraintException e) {
            e.printStackTrace();
        }

        Map<String, ConfigurationFieldModel> fieldMap = new HashMap<>();
        for (ConfigurationModel model : models) {
            fieldMap.putAll(model.getCopyOfKeyToFieldMap());
        }

        FieldUtility fieldUtility = new FieldUtility(fieldMap);
        String createdAt = DateUtils.formatDate(DateUtils.createCurrentDateTimestamp(), RestConstants.JSON_DATE_FORMAT);
        DistributionEvent event = new DistributionEvent(
            String.valueOf(distribution_config.getConfigurationId()), slackChannelKey.getUniversalKey(), createdAt, 1L, ProcessingType.DEFAULT.name(), MessageContentGroup.singleton(content),
            fieldUtility);
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
    public Map<String, String> createInvalidDistributionFieldMap() {
        return Map.of(SlackDescriptor.KEY_WEBHOOK, "",
            SlackDescriptor.KEY_CHANNEL_NAME, "");
    }

    @Override
    public FieldModel createTestConfigDestination() {
        return createFieldModel(new SlackChannelKey().getUniversalKey(), "");
    }

    @Override
    public String getTestJobName() {
        return UNIT_TEST_JOB_NAME;
    }

    @Override
    public String getDestinationName() {
        return slackChannelKey.getUniversalKey();
    }

    @Override
    public TestAction getTestAction() {
        return new ChannelDistributionTestAction(slackChannel) {};
    }

    @Override
    public void testGlobalConfig() {

    }

    @Override
    public void testGlobalValidate() {

    }

    @Override
    public void testGlobalValidateWithFieldErrors() {

    }

}
