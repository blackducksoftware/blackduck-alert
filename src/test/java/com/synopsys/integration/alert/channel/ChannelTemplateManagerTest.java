package com.synopsys.integration.alert.channel;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.jms.core.JmsTemplate;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.event.AlertEvent;
import com.synopsys.integration.alert.common.event.ContentEvent;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.database.audit.AuditUtility;
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
        final ContentConverter contentConverter = Mockito.mock(ContentConverter.class);
        Mockito.doNothing().when(jmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        final ChannelTemplateManager channelTemplateManager = new ChannelTemplateManager(contentConverter, auditUtility, jmsTemplate);

        final LinkableItem subTopic = new LinkableItem("subTopic", "sub topic", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "topic", null, subTopic, Collections.emptyList());
        final FieldAccessor fieldAccessor = new FieldAccessor(Map.of());
        final DistributionEvent event = new DistributionEvent("1L", "destination", RestConstants.formatDate(new Date()), "provider", "FORMAT",
            content, fieldAccessor);
        channelTemplateManager.sendEvents(List.of(event));
    }

    @Test
    public void testNotAbstractChannelEvent() {
        final AuditUtility auditUtility = Mockito.mock(AuditUtility.class);
        final JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
        final ContentConverter contentConverter = Mockito.mock(ContentConverter.class);
        Mockito.doNothing().when(jmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        final ChannelTemplateManager channelTemplateManager = new ChannelTemplateManager(contentConverter, auditUtility, jmsTemplate);
        final LinkableItem subTopic = new LinkableItem("subTopic", "sub topic", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "topic", null, subTopic, Collections.emptyList());
        final AlertEvent dbStoreEvent = new ContentEvent("", RestConstants.formatDate(new Date()), "", "FORMAT", content);
        final boolean isTrue = channelTemplateManager.sendEvent(dbStoreEvent);
        assertTrue(isTrue);
    }
}
