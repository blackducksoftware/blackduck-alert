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

    public ComponentItem createCategoryItem(String component, String componentValue, final ItemOperation operation, LinkableItem item) throws AlertException {
        ComponentItem.Builder componentBuilder = new ComponentItem.Builder();
        componentBuilder
            .applyComponentData(component, componentValue)
            .applyCategory("category")
            .applyNotificationId(1L)
            .applyOperation(operation)
            .applyComponentAttribute(item);

        return componentBuilder.build();
    }

    public ProviderMessageContent createProviderMessageContent(final String name, final String value, final ComponentItem... componentItems) throws AlertException {
        LinkableItem provider = new LinkableItem("Provider", "BlackDuck");
        LinkableItem topic = new LinkableItem(name, value);

        ProviderMessageContent.Builder builder = new ProviderMessageContent.Builder();
        builder.applyProvider("BlackDuck");
        builder.applyTopic(name, value);
        for (ComponentItem componentItem : componentItems) {
            builder.applyComponentItem(componentItem);
        }
        return builder.build();
    }

    public List<ProviderMessageContent> createDefaultMessages() throws AlertException {
        ComponentItem categoryItem1 = createCategoryItem("Category 1", "key1",
            ItemOperation.ADD, createLinkableItem("data 1", "value1")
        );
        ComponentItem categoryItem2 = createCategoryItem("Category 2", "key2",
            ItemOperation.ADD, createLinkableItem("data 2", "value2")
        );

        ComponentItem categoryItem3 = createCategoryItem("Category 1", "key1",
            ItemOperation.DELETE, createLinkableItem("data 1", "value1")
        );

        String topic1 = "Topic One";
        String value1 = "Value One";
        ProviderMessageContent aggregateMessageContent1 = createProviderMessageContent(topic1, value1, categoryItem1, categoryItem2, categoryItem3);

        ComponentItem categoryItem4 = createCategoryItem("Category 2", "key2",
            ItemOperation.DELETE, createLinkableItem("data 2", "value2")
        );

        ComponentItem categoryItem5 = createCategoryItem("Category 1", "key1",
            ItemOperation.ADD, createLinkableItem("data 1", "value1")
        );

        ProviderMessageContent aggregateMessageContent2 = createProviderMessageContent(topic1, value1, categoryItem4, categoryItem5);

        ProviderMessageContent aggregateMessageContent3 = createProviderMessageContent("Topic Two", "Value Two", categoryItem2);
        return List.of(aggregateMessageContent1, aggregateMessageContent2, aggregateMessageContent3);
    }

}
