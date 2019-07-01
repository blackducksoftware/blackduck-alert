package com.synopsys.integration.alert.common.workflow.processor;

import java.util.List;
import java.util.TreeSet;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.CategoryItem;
import com.synopsys.integration.alert.common.message.model.CategoryKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

public abstract class ProcessorTest {

    public LinkableItem createLinkableItem(String name, String value) {
        return new LinkableItem(name, value);
    }

    public CategoryItem createCategoryItem(final CategoryKey categoryKey, final ItemOperation operation, LinkableItem item) {
        return new CategoryItem(categoryKey, operation, null, item);
    }

    public AggregateMessageContent createAggregateMessageContent(final String name, final String value, final CategoryItem... categoryItems) {
        TreeSet items = new TreeSet();
        for (CategoryItem categoryItem : categoryItems) {
            items.add(categoryItem);
        }
        return new AggregateMessageContent(name, value, items);
    }

    public List<AggregateMessageContent> createDefaultMessages() {
        CategoryItem categoryItem1 = createCategoryItem(
            CategoryKey.from("Category 1", "key1"),
            ItemOperation.ADD, createLinkableItem("data 1", "value1")
        );
        CategoryItem categoryItem2 = createCategoryItem(
            CategoryKey.from("Category 2", "key2"),
            ItemOperation.ADD, createLinkableItem("data 2", "value2")
        );

        CategoryItem categoryItem3 = createCategoryItem(
            CategoryKey.from("Category 1", "key1"),
            ItemOperation.DELETE, createLinkableItem("data 1", "value1")
        );

        String topic1 = "Topic One";
        String value1 = "Value One";
        AggregateMessageContent aggregateMessageContent1 = createAggregateMessageContent(topic1, value1, categoryItem1, categoryItem2, categoryItem3);

        CategoryItem categoryItem4 = createCategoryItem(
            CategoryKey.from("Category 2", "key2"),
            ItemOperation.DELETE, createLinkableItem("data 2", "value2")
        );

        CategoryItem categoryItem5 = createCategoryItem(
            CategoryKey.from("Category 1", "key1"),
            ItemOperation.ADD, createLinkableItem("data 1", "value1")
        );

        AggregateMessageContent aggregateMessageContent2 = createAggregateMessageContent(topic1, value1, categoryItem4, categoryItem5);

        AggregateMessageContent aggregateMessageContent3 = createAggregateMessageContent("Topic Two", "Value Two", categoryItem2);
        return List.of(aggregateMessageContent1, aggregateMessageContent2, aggregateMessageContent3);
    }

}
