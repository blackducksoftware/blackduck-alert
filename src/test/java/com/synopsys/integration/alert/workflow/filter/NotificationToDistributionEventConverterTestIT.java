package com.synopsys.integration.alert.workflow.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.channel.event.NotificationToDistributionEventConverter;
import com.synopsys.integration.alert.common.configuration.CommonDistributionConfiguration;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationJobModel;
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class NotificationToDistributionEventConverterTestIT extends AlertIntegrationTest {

    @Autowired
    private DescriptorMap descriptorMap;

    @Autowired
    private BaseConfigurationAccessor configurationAccessor;

    @Test
    public void convertToEventsTest() {
        final NotificationToDistributionEventConverter converter = new NotificationToDistributionEventConverter(descriptorMap, configurationAccessor);
        final Map<CommonDistributionConfiguration, List<AggregateMessageContent>> messageContentMap = new HashMap<>();
        final List messageContent = List.of(createMessageContent("test"), createMessageContent("example"));

        messageContentMap.put(createEmailConfig(), messageContent);
        messageContentMap.put(createHipChatConfig(), messageContent);
        messageContentMap.put(createSlackConfig(), messageContent);

        final List<DistributionEvent> events = converter.convertToEvents(messageContentMap);
        assertEquals(6, events.size());
    }

    private CommonDistributionConfiguration createEmailConfig() {
        final ConfigurationJobModel model = Mockito.mock(ConfigurationJobModel.class);
        final List<ConfigurationFieldModel> fields = MockConfigurationModelFactory.createEmailDistributionFieldsProjectOwnerOnly();
        fields.addAll(MockConfigurationModelFactory.createBlackDuckDistributionFields());
        Mockito.when(model.createKeyToFieldMap()).thenReturn(MockConfigurationModelFactory.mapFieldKeyToFields(fields));
        Mockito.when(model.getJobId()).thenReturn(UUID.randomUUID());

        final CommonDistributionConfiguration config = new CommonDistributionConfiguration(model);
        return config;
    }

    private CommonDistributionConfiguration createHipChatConfig() {
        final ConfigurationJobModel model = Mockito.mock(ConfigurationJobModel.class);
        final List<ConfigurationFieldModel> fields = MockConfigurationModelFactory.createHipChatDistributionFields();
        fields.addAll(MockConfigurationModelFactory.createBlackDuckDistributionFields());
        Mockito.when(model.createKeyToFieldMap()).thenReturn(MockConfigurationModelFactory.mapFieldKeyToFields(fields));
        Mockito.when(model.getJobId()).thenReturn(UUID.randomUUID());

        final CommonDistributionConfiguration config = new CommonDistributionConfiguration(model);
        return config;
    }

    private CommonDistributionConfiguration createSlackConfig() {
        final ConfigurationJobModel model = Mockito.mock(ConfigurationJobModel.class);
        final List<ConfigurationFieldModel> fields = MockConfigurationModelFactory.createSlackDistributionFields();
        fields.addAll(MockConfigurationModelFactory.createBlackDuckDistributionFields());
        Mockito.when(model.createKeyToFieldMap()).thenReturn(MockConfigurationModelFactory.mapFieldKeyToFields(fields));
        Mockito.when(model.getJobId()).thenReturn(UUID.randomUUID());

        final CommonDistributionConfiguration config = new CommonDistributionConfiguration(model);
        return config;
    }

    private AggregateMessageContent createMessageContent(final String value) {
        return new AggregateMessageContent("Name", value, List.of());
    }

}
