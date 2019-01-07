package com.synopsys.integration.alert.channel.hipchat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.channel.ChannelDescriptorTest;
import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDescriptor;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.DateRange;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.DefinedFieldModel;
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;
import com.synopsys.integration.rest.RestConstants;

public class HipChatChannelDescriptorTestIT extends ChannelDescriptorTest {
    public static final String UNIT_TEST_JOB_NAME = "HipChatUnitTestJob";
    @Autowired
    private HipChatDescriptor hipChatDescriptor;

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
    public Optional<ConfigurationAccessor.ConfigurationModel> saveGlobalConfiguration() throws Exception {
        final Map<String, String> valueMap = new HashMap<>();
        final String apiToken = properties.getProperty(TestPropertyKey.TEST_HIPCHAT_API_KEY);
        valueMap.put(HipChatDescriptor.KEY_API_KEY, apiToken);
        final Map<String, ConfigurationFieldModel> fieldModelMap = MockConfigurationModelFactory.mapStringsToFields(valueMap);

        return Optional.of(configurationAccessor.createConfiguration(HipChatChannel.COMPONENT_NAME, ConfigContextEnum.GLOBAL, fieldModelMap.values()));
    }

    @Override
    public ConfigurationAccessor.ConfigurationModel saveDistributionConfiguration() throws Exception {
        final List<ConfigurationFieldModel> models = new LinkedList<>();
        models.addAll(MockConfigurationModelFactory.createHipChatDistributionFields());
        return configurationAccessor.createConfiguration(HipChatChannel.COMPONENT_NAME, ConfigContextEnum.DISTRIBUTION, models);
    }

    @Override
    public DistributionEvent createChannelEvent() {
        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "", null, subTopic, Collections.emptyList());
        List<ConfigurationAccessor.ConfigurationModel> models = List.of();
        try {
            models = configurationAccessor.getConfigurationsByDescriptorName(HipChatChannel.COMPONENT_NAME);
        } catch (final AlertDatabaseConstraintException e) {
            e.printStackTrace();
        }

        final Map<String, ConfigurationFieldModel> fieldMap = new HashMap<>();
        for (final ConfigurationAccessor.ConfigurationModel model : models) {
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
    public boolean assertGlobalFields(final Collection<DefinedFieldModel> globalFields) {
        boolean result = true;
        final Set<String> fieldNames = Set.of(HipChatDescriptor.KEY_API_KEY, HipChatDescriptor.KEY_HOST_SERVER);
        result = result && globalFields.stream().map(DefinedFieldModel::getKey).allMatch(fieldNames::contains);

        final Optional<DefinedFieldModel> apiKeyField = globalFields.stream()
                                                            .filter(field -> HipChatDescriptor.KEY_API_KEY.equals(field.getKey()))
                                                            .findFirst();
        if (apiKeyField.isPresent()) {
            result = result && apiKeyField.get().getSensitive();
        }
        return result;
    }

    @Override
    public boolean assertDistributionFields(final Collection<DefinedFieldModel> distributionFields) {
        final Set<String> fieldNames = Set.of(HipChatDescriptor.KEY_ROOM_ID, HipChatDescriptor.KEY_COLOR, HipChatDescriptor.KEY_NOTIFY);
        return distributionFields.stream().map(DefinedFieldModel::getKey).allMatch(fieldNames::contains);
    }

    @Override
    public Map<String, String> createInvalidGlobalFieldMap() {
        return Map.of(HipChatDescriptor.KEY_API_KEY, "");
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

    @Test
    public void testInvalidTextRoomID() {
        final Map<String, String> invalidValuesMap = new HashMap<>();
        invalidValuesMap.putAll(createInvalidCommonDistributionFieldMap());
        invalidValuesMap.putAll(Map.of(HipChatDescriptor.KEY_ROOM_ID, "abcdefg"));
        final Map<String, FieldValueModel> fieldModelMap = createFieldValueModelMap(invalidValuesMap);
        final FieldModel model = new FieldModel("1L", getDescriptor().getDestinationName(), ConfigContextEnum.DISTRIBUTION.name(), fieldModelMap);
        final HashMap<String, String> fieldErrors = new HashMap<>();
        final DescriptorActionApi descriptorActionApi = getDescriptor().getActionApi(ConfigContextEnum.DISTRIBUTION).get();
        final DescriptorActionApi spyDescriptorConfig = Mockito.spy(descriptorActionApi);
        spyDescriptorConfig.validateConfig(model.convertToFieldAccessor(), fieldErrors);
        assertEquals(model.getKeyToValues().size(), fieldErrors.size());
        Mockito.verify(spyDescriptorConfig).validateConfig(Mockito.any(), Mockito.anyMap());
    }
}
