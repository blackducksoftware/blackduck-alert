package com.synopsys.integration.alert.workflow.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.channel.event.NotificationToDistributionEventConverter;
import com.synopsys.integration.alert.common.configuration.CommonDistributionConfiguration;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationAccessor.ConfigurationModel;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class NotificationToDistributionEventConverterTestIT extends AlertIntegrationTest {

    @Autowired
    private DescriptorMap descriptorMap;

    @Test
    public void convertToEventsTest() {
        final NotificationToDistributionEventConverter converter = new NotificationToDistributionEventConverter(descriptorMap);
        final Map<CommonDistributionConfiguration, List<AggregateMessageContent>> messageContentMap = new HashMap<>();
        final List messageContent = List.of(createMessageContent("test"), createMessageContent("example"));

        messageContentMap.put(createEmailConfig(), messageContent);
        messageContentMap.put(createHipChatConfig(), messageContent);
        messageContentMap.put(createSlackConfig(), messageContent);

        final List<DistributionEvent> events = converter.convertToEvents(messageContentMap);
        assertEquals(6, events.size());
    }

    private CommonDistributionConfiguration createEmailConfig() {
        final ConfigurationModel model = Mockito.mock(ConfigurationModel.class);
        final List<ConfigurationFieldModel> fields = MockConfigurationModelFactory.createEmailDistributionFieldsProjectOwnerOnly();
        Mockito.when(model.getCopyOfFieldList()).thenReturn(fields);
        Mockito.when(model.getCopyOfKeyToFieldMap()).thenReturn(MockConfigurationModelFactory.mapFieldKeyToFields(fields));

        final CommonDistributionConfiguration config = new CommonDistributionConfiguration(model);
        return config;
    }

    private CommonDistributionConfiguration createHipChatConfig() {
        final ConfigurationModel model = Mockito.mock(ConfigurationModel.class);
        final List<ConfigurationFieldModel> fields = MockConfigurationModelFactory.createHipChatDistributionFields();
        Mockito.when(model.getCopyOfFieldList()).thenReturn(fields);
        Mockito.when(model.getCopyOfKeyToFieldMap()).thenReturn(MockConfigurationModelFactory.mapFieldKeyToFields(fields));

        final CommonDistributionConfiguration config = new CommonDistributionConfiguration(model);
        return config;
    }

    private CommonDistributionConfiguration createSlackConfig() {
        final ConfigurationModel model = Mockito.mock(ConfigurationModel.class);
        final List<ConfigurationFieldModel> fields = MockConfigurationModelFactory.createSlackDistributionFields();
        Mockito.when(model.getCopyOfFieldList()).thenReturn(MockConfigurationModelFactory.createSlackDistributionFields());
        Mockito.when(model.getCopyOfKeyToFieldMap()).thenReturn(MockConfigurationModelFactory.mapFieldKeyToFields(fields));
        final CommonDistributionConfiguration config = new CommonDistributionConfiguration(model);
        return config;
    }

    private AggregateMessageContent createMessageContent(final String value) {
        return new AggregateMessageContent("Name", value, List.of());
    }

}
