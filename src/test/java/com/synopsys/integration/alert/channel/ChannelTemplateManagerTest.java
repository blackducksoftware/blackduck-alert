package com.synopsys.integration.alert.channel;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.jms.core.JmsTemplate;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannelEvent;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.event.AlertEvent;
import com.synopsys.integration.alert.common.event.ContentEvent;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.database.api.AuditUtility;
import com.synopsys.integration.rest.RestConstants;

public class ChannelTemplateManagerTest {
    private Gson gson;
    private ContentConverter contentConverter;

    @Before
    public void init() {
        gson = new Gson();
        contentConverter = new ContentConverter(gson, new DefaultConversionService());
    }

    @Test
    public void testSendEvents() {
        final AuditUtility auditUtility = Mockito.mock(AuditUtility.class);
        final JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
        Mockito.doNothing().when(jmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        final ChannelTemplateManager channelTemplateManager = new ChannelTemplateManager(gson, auditUtility, jmsTemplate);

        final LinkableItem subTopic = new LinkableItem("subTopic", "sub topic", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "topic", null, subTopic, Collections.emptyList());
        final HipChatChannelEvent hipChatEvent = new HipChatChannelEvent(RestConstants.formatDate(new Date()), "provider", "FORMAT",
            content, 1L, 20, false, "red");
        channelTemplateManager.sendEvents(Arrays.asList(hipChatEvent));
    }

    @Test
    public void testNotAbstractChannelEvent() {
        final AuditUtility auditUtility = Mockito.mock(AuditUtility.class);
        final JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
        Mockito.doNothing().when(jmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        final ChannelTemplateManager channelTemplateManager = new ChannelTemplateManager(gson, auditUtility, jmsTemplate);
        final LinkableItem subTopic = new LinkableItem("subTopic", "sub topic", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "topic", null, subTopic, Collections.emptyList());
        final AlertEvent dbStoreEvent = new ContentEvent("", RestConstants.formatDate(new Date()), "", "FORMAT", content);
        final boolean isTrue = channelTemplateManager.sendEvent(dbStoreEvent);
        assertTrue(isTrue);
    }
}
