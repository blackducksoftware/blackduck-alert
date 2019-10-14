package com.synopsys.integration.alert.workflow.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.workflow.processor.NotificationToDistributionEventConverter;
import com.synopsys.integration.alert.mock.MockConfigurationModelFactory;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class NotificationToDistributionEventConverterTestIT extends AlertIntegrationTest {

    @Autowired
    private ConfigurationAccessor configurationAccessor;

    @Test
    public void convertToEventsTest() throws Exception {
        final DescriptorMap descriptorMap = new DescriptorMap(List.of(), List.of(), List.of(), List.of());
        final NotificationToDistributionEventConverter converter = new NotificationToDistributionEventConverter(configurationAccessor, descriptorMap);
        final List<MessageContentGroup> messageContentGroups = new ArrayList<>();
        final MessageContentGroup contentGroup1 = MessageContentGroup.singleton(createMessageContent("test"));
        final MessageContentGroup contentGroup2 = MessageContentGroup.singleton(createMessageContent("example"));
        messageContentGroups.add(contentGroup1);
        messageContentGroups.add(contentGroup2);

        final List<DistributionEvent> emailEvents = converter.convertToEvents(createEmailConfig(), messageContentGroups);
        final List<DistributionEvent> slackEvents = converter.convertToEvents(createSlackConfig(), messageContentGroups);
        assertEquals(4, emailEvents.size() + slackEvents.size());
    }

    private ConfigurationJobModel createEmailConfig() {
        final List<ConfigurationFieldModel> fields = MockConfigurationModelFactory.createEmailDistributionFieldsProjectOwnerOnly();
        fields.addAll(MockConfigurationModelFactory.createBlackDuckDistributionFields());
        return MockConfigurationModelFactory.createDistributionJob(fields);
    }

    private ConfigurationJobModel createSlackConfig() {
        final List<ConfigurationFieldModel> fields = MockConfigurationModelFactory.createSlackDistributionFields();
        fields.addAll(MockConfigurationModelFactory.createBlackDuckDistributionFields());
        return MockConfigurationModelFactory.createDistributionJob(fields);
    }

    private ProviderMessageContent createMessageContent(final String value) throws AlertException {
        return new ProviderMessageContent.Builder().applyProvider("testProvider").applyTopic("Name", value).build();
    }

}
