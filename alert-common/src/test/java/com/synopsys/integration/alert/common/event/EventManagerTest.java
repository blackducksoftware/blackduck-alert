package com.synopsys.integration.alert.common.event;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jms.core.JmsTemplate;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.rest.RestConstants;

public class EventManagerTest {

    @Test
    public void testSendEvents() throws Exception {
        final JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
        final ContentConverter contentConverter = Mockito.mock(ContentConverter.class);
        Mockito.doNothing().when(jmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        final EventManager eventManager = new EventManager(contentConverter, jmsTemplate);

        final LinkableItem subTopic = new LinkableItem("subTopic", "sub topic", null);
        final ProviderMessageContent content = new ProviderMessageContent.Builder()
                                                   .applyProvider("provider")
                                                   .applyTopic("testTopic", "topic")
                                                   .applySubTopic(subTopic.getName(), subTopic.getValue())
                                                   .build();
        final FieldAccessor fieldAccessor = new FieldAccessor(Map.of());
        final DistributionEvent event = new DistributionEvent(UUID.randomUUID().toString(), "destination", RestConstants.formatDate(new Date()), "provider", "FORMAT",
            MessageContentGroup.singleton(content), fieldAccessor);
        eventManager.sendEvents(List.of(event));
    }

    @Test
    public void testNotAbstractChannelEvent() throws Exception {
        final JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
        final ContentConverter contentConverter = Mockito.mock(ContentConverter.class);
        Mockito.doNothing().when(jmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        final EventManager eventManager = new EventManager(contentConverter, jmsTemplate);
        final LinkableItem subTopic = new LinkableItem("subTopic", "sub topic", null);
        final ProviderMessageContent content = new ProviderMessageContent.Builder()
                                                   .applyProvider("provider")
                                                   .applyTopic("testTopic", "topic")
                                                   .applySubTopic(subTopic.getName(), subTopic.getValue())
                                                   .build();
        final AlertEvent dbStoreEvent = new ContentEvent("", RestConstants.formatDate(new Date()), "", "FORMAT", MessageContentGroup.singleton(content));
        eventManager.sendEvent(dbStoreEvent);
    }
}
