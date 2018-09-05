package com.synopsys.integration.alert.common.event;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class TopicEventTest {

    @Test
    public void testTopicList() {
        final String topic = "TOPIC";
        final List<TopicContent> contentList = Collections.emptyList();
        final TopicEvent topicEvent = new TopicEvent(topic, 1L, contentList);
        assertEquals(contentList, topicEvent.getTopicList());
    }
}
