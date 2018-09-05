package com.synopsys.integration.alert.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

public class TopicContentTest {

    @Test
    public void testTopicContentWithURL() {
        final String name = "topic name";
        final String value = "topic value";
        final String url = "topic url";
        final LinkableItem subTopic = Mockito.mock(LinkableItem.class);
        final List<CategoryItem> itemList = Collections.emptyList();
        final TopicContent topicContent = new TopicContent(name, value, url, subTopic, itemList);

        assertEquals(name, topicContent.getName());
        assertEquals(value, topicContent.getValue());
        assertTrue(topicContent.getUrl().isPresent());
        assertEquals(url, topicContent.getUrl().get());
        assertTrue(topicContent.getSubTopic().isPresent());
        assertEquals(subTopic, topicContent.getSubTopic().get());
        assertEquals(itemList, topicContent.getCategoryItemList());
    }

    @Test
    public void testTopicContentMissingURLandSubTopic() {
        final String name = "topic name";
        final String value = "topic value";
        final List<CategoryItem> itemList = Collections.emptyList();
        final TopicContent topicContent = new TopicContent(name, value, itemList);

        assertEquals(name, topicContent.getName());
        assertEquals(value, topicContent.getValue());
        assertFalse(topicContent.getUrl().isPresent());
        assertFalse(topicContent.getSubTopic().isPresent());
        assertEquals(itemList, topicContent.getCategoryItemList());
    }

    @Test
    public void testTopicContentMissingSubTopic() {
        final String name = "topic name";
        final String value = "topic value";
        final String url = "topic url";
        final List<CategoryItem> itemList = Collections.emptyList();
        final TopicContent topicContent = new TopicContent(name, value, url, itemList);

        assertEquals(name, topicContent.getName());
        assertEquals(value, topicContent.getValue());
        assertTrue(topicContent.getUrl().isPresent());
        assertEquals(url, topicContent.getUrl().get());
        assertFalse(topicContent.getSubTopic().isPresent());
        assertEquals(itemList, topicContent.getCategoryItemList());
    }
}
