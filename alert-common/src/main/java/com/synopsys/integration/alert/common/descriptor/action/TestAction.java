package com.synopsys.integration.alert.common.descriptor.action;

import java.util.TreeSet;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.rest.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

public abstract class TestAction {
    private final String descriptorName;
    private final ConfigContextEnum context;

    protected TestAction(final String descriptorName, final ConfigContextEnum context) {
        this.descriptorName = descriptorName;
        this.context = context;
    }

    public TestConfigModel createTestConfigModel(final String configId, final FieldAccessor fieldAccessor, final String destination) throws IntegrationException {
        final TestConfigModel testConfigModel = new TestConfigModel(fieldAccessor, destination);
        testConfigModel.setConfigId(configId);
        return testConfigModel;
    }

    public abstract void testConfig(final TestConfigModel testConfig) throws IntegrationException;

    public AggregateMessageContent createTestNotificationContent() {
        final LinkableItem subTopic = new LinkableItem("subTopic", "Test message sent by Alert", null);
        return new AggregateMessageContent("testTopic", "Alert Test Message", null, subTopic, new TreeSet<>());
    }

    public String getDescriptorName() {
        return descriptorName;
    }

    public ConfigContextEnum getContext() {
        return context;
    }
}
