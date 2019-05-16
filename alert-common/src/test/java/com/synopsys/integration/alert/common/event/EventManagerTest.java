package com.synopsys.integration.alert.common.event;

import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jms.core.JmsTemplate;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.accessor.AuditUtility;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.rest.RestConstants;

public class EventManagerTest {

    @Test
    public void testSendEvents() {
        final AuditUtility auditUtility = Mockito.mock(AuditUtility.class);
        final JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
        final ContentConverter contentConverter = Mockito.mock(ContentConverter.class);
        Mockito.doNothing().when(jmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        final EventManager eventManager = new EventManager(contentConverter, auditUtility, jmsTemplate);

        final LinkableItem subTopic = new LinkableItem("subTopic", "sub topic", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "topic", null, subTopic, Collections.emptyList());
        final FieldAccessor fieldAccessor = new FieldAccessor(Map.of());
        final DistributionEvent event = new DistributionEvent(UUID.randomUUID().toString(), "destination", RestConstants.formatDate(new Date()), "provider", "FORMAT",
            content, fieldAccessor);
        eventManager.sendEvents(List.of(event));
    }

    @Test
    public void testNotAbstractChannelEvent() {
        final AuditUtility auditUtility = Mockito.mock(AuditUtility.class);
        final JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
        final ContentConverter contentConverter = Mockito.mock(ContentConverter.class);
        Mockito.doNothing().when(jmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        final EventManager eventManager = new EventManager(contentConverter, auditUtility, jmsTemplate);
        final LinkableItem subTopic = new LinkableItem("subTopic", "sub topic", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "topic", null, subTopic, Collections.emptyList());
        final AlertEvent dbStoreEvent = new ContentEvent("", RestConstants.formatDate(new Date()), "", "FORMAT", content);
        final boolean isTrue = eventManager.sendEvent(dbStoreEvent);
        assertTrue(isTrue);
    }
}
