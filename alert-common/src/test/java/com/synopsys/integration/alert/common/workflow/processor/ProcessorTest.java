package com.synopsys.integration.alert.common.workflow.processor;

import java.util.List;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

public abstract class ProcessorTest {

    public LinkableItem createLinkableItem(String name, String value) {
        return new LinkableItem(name, value);
    }

    public ComponentItem createComponentItem(String component, String componentValue, ItemOperation operation, LinkableItem categoryItem) throws AlertException {
        ComponentItem.Builder componentBuilder = new ComponentItem.Builder();
        componentBuilder
            .applyCategory("category")
            .applyOperation(operation)
            .applyComponentData(component, componentValue)
            .applyCategoryItem(categoryItem)
            .applyNotificationId(1L);

        return componentBuilder.build();
    }

    public ProviderMessageContent createProviderMessageContent(String name, String value, ComponentItem... componentItems) throws AlertException {
        ProviderMessageContent.Builder builder = new ProviderMessageContent.Builder()
                                                     .applyProvider("Black Duck", 1L)
                                                     .applyTopic(name, value);
        for (ComponentItem componentItem : componentItems) {
            builder.applyComponentItem(componentItem);
        }
        return builder.build();
    }

    public List<ProviderMessageContent> createDefaultMessages() throws AlertException {
        ComponentItem categoryItem1 = createComponentItem("Category 1", "key1",
            ItemOperation.ADD, createLinkableItem("data 1", "value1")
        );
        ComponentItem categoryItem2 = createComponentItem("Category 2", "key2",
            ItemOperation.ADD, createLinkableItem("data 2", "value2")
        );

        ComponentItem categoryItem3 = createComponentItem("Category 1", "key1",
            ItemOperation.DELETE, createLinkableItem("data 1", "value1")
        );

        String topic1 = "Topic One";
        String value1 = "Value One";
        ProviderMessageContent aggregateMessageContent1 = createProviderMessageContent(topic1, value1, categoryItem1, categoryItem2, categoryItem3);

        ComponentItem categoryItem4 = createComponentItem("Category 2", "key2",
            ItemOperation.DELETE, createLinkableItem("data 2", "value2")
        );

        ComponentItem categoryItem5 = createComponentItem("Category 1", "key1",
            ItemOperation.ADD, createLinkableItem("data 1", "value1")
        );

        ProviderMessageContent aggregateMessageContent2 = createProviderMessageContent(topic1, value1, categoryItem4, categoryItem5);

        ProviderMessageContent aggregateMessageContent3 = createProviderMessageContent("Topic Two", "Value Two", categoryItem2);
        return List.of(aggregateMessageContent1, aggregateMessageContent2, aggregateMessageContent3);
    }

}
