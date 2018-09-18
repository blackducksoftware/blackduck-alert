package com.synopsys.integration.alert.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

public class AggregateMessageContentTest {

    @Test
    public void testTopicContentWithURL() {
        final String name = "topic name";
        final String value = "topic value";
        final String url = "topic url";
        final LinkableItem subTopic = Mockito.mock(LinkableItem.class);
        final List<CategoryItem> itemList = Collections.emptyList();
        final AggregateMessageContent aggregateMessageContent = new AggregateMessageContent(name, value, url, subTopic, itemList);

        assertEquals(name, aggregateMessageContent.getName());
        assertEquals(value, aggregateMessageContent.getValue());
        assertTrue(aggregateMessageContent.getUrl().isPresent());
        assertEquals(url, aggregateMessageContent.getUrl().get());
        assertTrue(aggregateMessageContent.getSubTopic().isPresent());
        assertEquals(subTopic, aggregateMessageContent.getSubTopic().get());
        assertEquals(itemList, aggregateMessageContent.getCategoryItemList());
    }

    @Test
    public void testTopicContentMissingURLandSubTopic() {
        final String name = "topic name";
        final String value = "topic value";
        final List<CategoryItem> itemList = Collections.emptyList();
        final AggregateMessageContent aggregateMessageContent = new AggregateMessageContent(name, value, itemList);

        assertEquals(name, aggregateMessageContent.getName());
        assertEquals(value, aggregateMessageContent.getValue());
        assertFalse(aggregateMessageContent.getUrl().isPresent());
        assertFalse(aggregateMessageContent.getSubTopic().isPresent());
        assertEquals(itemList, aggregateMessageContent.getCategoryItemList());
    }

    @Test
    public void testTopicContentMissingSubTopic() {
        final String name = "topic name";
        final String value = "topic value";
        final String url = "topic url";
        final List<CategoryItem> itemList = Collections.emptyList();
        final AggregateMessageContent aggregateMessageContent = new AggregateMessageContent(name, value, url, itemList);

        assertEquals(name, aggregateMessageContent.getName());
        assertEquals(value, aggregateMessageContent.getValue());
        assertTrue(aggregateMessageContent.getUrl().isPresent());
        assertEquals(url, aggregateMessageContent.getUrl().get());
        assertFalse(aggregateMessageContent.getSubTopic().isPresent());
        assertEquals(itemList, aggregateMessageContent.getCategoryItemList());
    }
}
