package com.synopsys.integration.alert.channel.hipchat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.channel.ChannelDescriptorTest;
import com.synopsys.integration.alert.channel.hipchat.actions.HipChatDistributionTestAction;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDescriptor;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.web.config.FieldValidationAction;
import com.synopsys.integration.rest.RestConstants;

public class HipChatChannelDescriptorTestIT extends ChannelDescriptorTest {
    public static final String UNIT_TEST_JOB_NAME = "HipChatUnitTestJob";
    @Autowired
    private HipChatDescriptor hipChatDescriptor;

    @Autowired
    private HipChatChannel hipChatChannel;

    @BeforeEach
    public void testSetup() throws Exception {
        final String blackDuckTimeoutKey = BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT;
        final ConfigurationFieldModel blackDuckTimeoutField = ConfigurationFieldModel.create(blackDuckTimeoutKey);
        blackDuckTimeoutField.setFieldValue(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TIMEOUT));

        final String blackDuckApiKey = BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY;
        final ConfigurationFieldModel blackDuckApiField = ConfigurationFieldModel.create(blackDuckApiKey);
        blackDuckApiField.setFieldValue(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_API_KEY));

        final String blackDuckProviderUrlKey = BlackDuckDescriptor.KEY_BLACKDUCK_URL;
        final ConfigurationFieldModel blackDuckProviderUrlField = ConfigurationFieldModel.create(blackDuckProviderUrlKey);
        blackDuckProviderUrlField.setFieldValue(properties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL));

        provider_global = configurationAccessor
                              .createConfiguration(BlackDuckProvider.COMPONENT_NAME, ConfigContextEnum.GLOBAL, List.of(blackDuckTimeoutField, blackDuckApiField, blackDuckProviderUrlField));
    }

    @Override
    public Optional<ConfigurationModel> saveGlobalConfiguration() throws Exception {
        final Map<String, String> valueMap = new HashMap<>();
        final String apiToken = properties.getProperty(TestPropertyKey.TEST_HIPCHAT_API_KEY);
        final String hipChatServerUrl = properties.getProperty(TestPropertyKey.TEST_HIPCHAT_SERVER_URL);
        valueMap.put(HipChatDescriptor.KEY_API_KEY, apiToken);
        valueMap.put(HipChatDescriptor.KEY_HOST_SERVER, hipChatServerUrl);
        final Map<String, ConfigurationFieldModel> fieldModelMap = MockConfigurationModelFactory.mapStringsToFields(valueMap);

        return Optional.of(configurationAccessor.createConfiguration(HipChatChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL, fieldModelMap.values()));
    }

    @Override
    public ConfigurationModel saveDistributionConfiguration() throws Exception {
        final List<ConfigurationFieldModel> models = new LinkedList<>();
        models.addAll(MockConfigurationModelFactory.createHipChatDistributionFields());
        return configurationAccessor.createConfiguration(HipChatChannel.COMPONENT_NAME, ConfigContextEnum.DISTRIBUTION, models);
    }

    @Override
    public DistributionEvent createChannelEvent() {
        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "", null, subTopic, new TreeSet<>());
        List<ConfigurationModel> models = List.of();
        try {
            models = configurationAccessor.getConfigurationsByDescriptorName(HipChatChannel.COMPONENT_NAME);
        } catch (final AlertDatabaseConstraintException e) {
            e.printStackTrace();
        }

        final Map<String, ConfigurationFieldModel> fieldMap = new HashMap<>();
        for (final ConfigurationModel model : models) {
            fieldMap.putAll(model.getCopyOfKeyToFieldMap());
        }

        final FieldAccessor fieldAccessor = new FieldAccessor(fieldMap);
        final String createdAt = RestConstants.formatDate(DateRange.createCurrentDateTimestamp());
        final DistributionEvent event = new DistributionEvent(String.valueOf(distribution_config.getConfigurationId()), HipChatChannel.COMPONENT_NAME, createdAt, BlackDuckProvider.COMPONENT_NAME, FormatType.DEFAULT.name(), content,
            fieldAccessor);
        return event;
    }

    @Override
    public ChannelDescriptor getDescriptor() {
        return hipChatDescriptor;
    }

    @Override
    public boolean assertGlobalFields(final Set<DefinedFieldModel> globalFields) {
        boolean result = true;
        final Set<String> fieldNames = Set.of(HipChatDescriptor.KEY_API_KEY, HipChatDescriptor.KEY_HOST_SERVER);
        result = result && globalFields
                               .stream()
                               .map(DefinedFieldModel::getKey)
                               .allMatch(fieldNames::contains);

        final Optional<DefinedFieldModel> apiKeyField = globalFields
                                                            .stream()
                                                            .filter(field -> HipChatDescriptor.KEY_API_KEY.equals(field.getKey()))
                                                            .findFirst();
        if (apiKeyField.isPresent()) {
            result = result && apiKeyField.get().getSensitive();
        }
        return result;
    }

    @Override
    public boolean assertDistributionFields(final Set<DefinedFieldModel> distributionFields) {
        final Set<String> fieldNames = Set.of(HipChatDescriptor.KEY_ROOM_ID, HipChatDescriptor.KEY_COLOR, HipChatDescriptor.KEY_NOTIFY);
        final Set<String> passedFieldNames = distributionFields.stream().map(DefinedFieldModel::getKey).collect(Collectors.toSet());
        return passedFieldNames.containsAll(fieldNames);
    }

    @Override
    public Map<String, String> createInvalidGlobalFieldMap() {
        return Map.of(HipChatDescriptor.KEY_API_KEY, "", HipChatDescriptor.KEY_HOST_SERVER, "");
    }

    @Override
    public Map<String, String> createInvalidDistributionFieldMap() {
        return Map.of(HipChatDescriptor.KEY_ROOM_ID, "");
    }

    @Override
    public String createTestConfigDestination() {
        return properties.getProperty(TestPropertyKey.TEST_HIPCHAT_ROOM_ID.getPropertyKey());
    }

    @Override
    public String getTestJobName() {
        return UNIT_TEST_JOB_NAME;
    }

    @Override
    public String getDestinationName() {
        return HipChatChannel.COMPONENT_NAME;
    }

    @Override
    public TestAction getTestAction() {
        return new HipChatDistributionTestAction(hipChatChannel);
    }

    @Test
    public void testInvalidTextRoomID() {
        final Map<String, String> invalidValuesMap = new HashMap<>();
        invalidValuesMap.putAll(Map.of(HipChatDescriptor.KEY_ROOM_ID, "abcdefg"));
        final int invalidLength = invalidValuesMap.size();
        invalidValuesMap.putAll(createValidCommonDistributionFieldMap());
        final Map<String, FieldValueModel> fieldModelMap = createFieldValueModelMap(invalidValuesMap);
        final FieldModel model = new FieldModel("1L", getDestinationName(), ConfigContextEnum.DISTRIBUTION.name(), fieldModelMap);
        final HashMap<String, String> fieldErrors = new HashMap<>();
        final Map<String, ConfigField> configFieldMap = getDescriptor().getUIConfig(ConfigContextEnum.DISTRIBUTION).get().createFields().stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        final FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, model, fieldErrors);
        assertEquals(invalidLength, fieldErrors.size());
    }
}
