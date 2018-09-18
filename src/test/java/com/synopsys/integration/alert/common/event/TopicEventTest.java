package com.synopsys.integration.alert.common.event;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.synopsys.integration.alert.common.model.AggregateMessageContent;

public class TopicEventTest {

    @Test
    public void testTopicList() {
        final String topic = "TOPIC";
        final List<AggregateMessageContent> contentList = Collections.emptyList();
        final TopicEvent topicEvent = new TopicEvent(topic, contentList);
        assertEquals(contentList, topicEvent.getTopicList());
    }
}
