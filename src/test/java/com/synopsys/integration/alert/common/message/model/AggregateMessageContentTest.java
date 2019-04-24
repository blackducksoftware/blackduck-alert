package com.synopsys.integration.alert.common.message.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AggregateMessageContentTest {

    @Test
    public void testTopicContentWithURL() {
        final String name = "topic name";
        final String value = "topic value";
        final String url = "topic url";
        final LinkableItem subTopic = Mockito.mock(LinkableItem.class);
        final SortedSet<CategoryItem> categoryItems = new TreeSet<>();
        final AggregateMessageContent aggregateMessageContent = new AggregateMessageContent(name, value, url, subTopic, categoryItems);

        assertEquals(name, aggregateMessageContent.getName());
        assertEquals(value, aggregateMessageContent.getValue());
        assertTrue(aggregateMessageContent.getUrl().isPresent());
        assertEquals(url, aggregateMessageContent.getUrl().get());
        assertTrue(aggregateMessageContent.getSubTopic().isPresent());
        assertEquals(subTopic, aggregateMessageContent.getSubTopic().get());
        assertEquals(categoryItems, aggregateMessageContent.getCategoryItems());
    }

    @Test
    public void testTopicContentMissingURLandSubTopic() {
        final String name = "topic name";
        final String value = "topic value";
        final SortedSet<CategoryItem> categoryItems = new TreeSet<>();
        final AggregateMessageContent aggregateMessageContent = new AggregateMessageContent(name, value, categoryItems);

        assertEquals(name, aggregateMessageContent.getName());
        assertEquals(value, aggregateMessageContent.getValue());
        assertFalse(aggregateMessageContent.getUrl().isPresent());
        assertFalse(aggregateMessageContent.getSubTopic().isPresent());
        assertEquals(categoryItems, aggregateMessageContent.getCategoryItems());
    }

    @Test
    public void testTopicContentMissingSubTopic() {
        final String name = "topic name";
        final String value = "topic value";
        final String url = "topic url";
        final SortedSet<CategoryItem> categoryItems = new TreeSet<>();
        final AggregateMessageContent aggregateMessageContent = new AggregateMessageContent(name, value, url, categoryItems);

        assertEquals(name, aggregateMessageContent.getName());
        assertEquals(value, aggregateMessageContent.getValue());
        assertTrue(aggregateMessageContent.getUrl().isPresent());
        assertEquals(url, aggregateMessageContent.getUrl().get());
        assertFalse(aggregateMessageContent.getSubTopic().isPresent());
        assertEquals(categoryItems, aggregateMessageContent.getCategoryItems());
    }
}
