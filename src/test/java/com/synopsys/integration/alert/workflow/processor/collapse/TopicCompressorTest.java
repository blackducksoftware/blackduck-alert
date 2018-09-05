package com.synopsys.integration.alert.workflow.processor.collapse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.model.CategoryItem;
import com.synopsys.integration.alert.common.model.CategoryKey;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.common.model.TopicContent;

public class TopicCompressorTest {

    @Test
    public void testAddOperation() {

        final String name = "Project_Topic";
        final String value = "Test Project";
        final String url = "test project url";
        final CategoryKey categoryKey = CategoryKey.from("notification_type", "component", "component_version");
        final LinkableItem itemData = new LinkableItem("vuln_id", "cwe-12345", "cwe url");
        final CategoryItem categoryItem = new CategoryItem(categoryKey, ItemOperation.ADD, Arrays.asList(itemData));

        final TopicContent topic = new TopicContent(name, value, url, Arrays.asList(categoryItem));
        final List<TopicContent> expectedContentList = Arrays.asList(topic);
        final TopicCompressor compressor = new TopicCompressor();
        final List<TopicContent> contentList = compressor.collapseTopics(Arrays.asList(topic));

        assertFalse(contentList.isEmpty());
        assertEquals(expectedContentList, contentList);
    }

    @Test
    public void testUpdateOperation() {
        final String name = "Project_Topic";
        final String value = "Test Project";
        final String url = "test project url";
        final CategoryKey categoryKey = CategoryKey.from("notification_type", "component", "component_version");
        final LinkableItem itemData = new LinkableItem("vuln_id", "cwe-12345", "cwe url");
        final CategoryItem categoryItem = new CategoryItem(categoryKey, ItemOperation.UPDATE, Arrays.asList(itemData));

        final TopicContent topic = new TopicContent(name, value, url, Arrays.asList(categoryItem));
        final List<TopicContent> expectedContentList = Arrays.asList(topic);
        final TopicCompressor compressor = new TopicCompressor();
        final List<TopicContent> contentList = compressor.collapseTopics(Arrays.asList(topic));

        assertFalse(contentList.isEmpty());
        assertEquals(expectedContentList, contentList);
    }

    @Test
    public void testDeleteOperationAdded() {
        final String name = "Project_Topic";
        final String value = "Test Project";
        final String url = "test project url";
        final CategoryKey categoryKey = CategoryKey.from("notification_type", "component", "component_version");
        final LinkableItem itemData = new LinkableItem("vuln_id", "cwe-12345", "cwe url");
        final CategoryItem categoryItem = new CategoryItem(categoryKey, ItemOperation.DELETE, Arrays.asList(itemData));

        final TopicContent topic = new TopicContent(name, value, url, Arrays.asList(categoryItem));
        final List<TopicContent> expectedContentList = Arrays.asList(topic);
        final TopicCompressor compressor = new TopicCompressor();
        final List<TopicContent> contentList = compressor.collapseTopics(Arrays.asList(topic));

        assertFalse(contentList.isEmpty());
        assertEquals(expectedContentList, contentList);
    }

    @Test
    public void testAddandDeleteOperation() {
        final String name = "Project_Topic";
        final String value = "Test Project";
        final String url = "test project url";
        final CategoryKey categoryKeyAdd = CategoryKey.from("notification_type", "component", "component_version");
        final LinkableItem itemDataAdd = new LinkableItem("vuln_id", "cwe-12345", "cwe url");
        final CategoryItem categoryItemAdd = new CategoryItem(categoryKeyAdd, ItemOperation.ADD, Arrays.asList(itemDataAdd));

        final CategoryKey categoryKeyDelete = CategoryKey.from("notification_type", "component", "component_version");
        final LinkableItem itemData = new LinkableItem("vuln_id", "cwe-12345", "cwe url");
        final CategoryItem categoryItemDelete = new CategoryItem(categoryKeyDelete, ItemOperation.DELETE, Arrays.asList(itemData));

        final TopicContent topic = new TopicContent(name, value, url, Arrays.asList(categoryItemAdd, categoryItemDelete));
        final List<TopicContent> expectedContentList = Arrays.asList(topic);
        final TopicCompressor compressor = new TopicCompressor();
        final List<TopicContent> contentList = compressor.collapseTopics(Arrays.asList(topic));

        assertTrue(contentList.isEmpty());
    }
}
