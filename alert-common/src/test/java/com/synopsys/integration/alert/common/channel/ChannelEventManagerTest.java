package com.synopsys.integration.alert.common.channel;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jms.core.JmsTemplate;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.event.AlertEvent;
import com.synopsys.integration.alert.common.event.ContentEvent;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.rest.RestConstants;

public class ChannelEventManagerTest {
    @Test
    public void testSendEvents() throws Exception {
        AuditAccessor auditAccessor = Mockito.mock(AuditAccessor.class);
        JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
        ContentConverter contentConverter = Mockito.mock(ContentConverter.class);
        Mockito.doNothing().when(jmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        ChannelEventManager eventManager = new ChannelEventManager(contentConverter, jmsTemplate, auditAccessor);

        LinkableItem subTopic = new LinkableItem("subTopic", "sub topic", null);
        ProviderMessageContent content = new ProviderMessageContent.Builder()
                                             .applyProvider("testProvider", 1L, "testProviderConfig")
                                             .applyTopic("testTopic", "topic")
                                             .applySubTopic(subTopic.getName(), subTopic.getValue())
                                             .build();
        FieldUtility fieldUtility = new FieldUtility(Map.of());
        DistributionEvent event = new DistributionEvent(UUID.randomUUID().toString(), "destination", RestConstants.formatDate(new Date()), 1L, "FORMAT",
            MessageContentGroup.singleton(content), fieldUtility);
        eventManager.sendEvents(List.of(event));
    }

    @Test
    public void testNotAbstractChannelEvent() throws Exception {
        AuditAccessor auditAccessor = Mockito.mock(AuditAccessor.class);
        JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
        ContentConverter contentConverter = Mockito.mock(ContentConverter.class);
        Mockito.doNothing().when(jmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        ChannelEventManager eventManager = new ChannelEventManager(contentConverter, jmsTemplate, auditAccessor);
        LinkableItem subTopic = new LinkableItem("subTopic", "sub topic", null);
        ProviderMessageContent content = new ProviderMessageContent.Builder()
                                             .applyProvider("testProvider", 1L, "testProviderConfig")
                                             .applyTopic("testTopic", "topic")
                                             .applySubTopic(subTopic.getName(), subTopic.getValue())
                                             .build();
        AlertEvent dbStoreEvent = new ContentEvent("", RestConstants.formatDate(new Date()), 1L, "FORMAT", MessageContentGroup.singleton(content));
        eventManager.sendEvent(dbStoreEvent);
    }

}
