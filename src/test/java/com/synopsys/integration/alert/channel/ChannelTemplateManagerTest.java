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
import com.synopsys.integration.alert.audit.mock.MockAuditEntryEntity;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannelEvent;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.event.AlertEvent;
import com.synopsys.integration.alert.common.event.ContentEvent;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
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
        final MockAuditEntryEntity mockAuditEntryEntity = new MockAuditEntryEntity();
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepository.save(Mockito.any(AuditEntryEntity.class))).thenReturn(mockAuditEntryEntity.createEntity());
        final AuditNotificationRepository auditNotificationRepositoryWrapper = Mockito.mock(AuditNotificationRepository.class);
        final JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
        Mockito.doNothing().when(jmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        final ChannelTemplateManager channelTemplateManager = new ChannelTemplateManager(gson, auditEntryRepository, auditNotificationRepositoryWrapper, jmsTemplate);

        final LinkableItem subTopic = new LinkableItem("subTopic", "sub topic", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "topic", null, subTopic, Collections.emptyList());
        final HipChatChannelEvent hipChatEvent = new HipChatChannelEvent(RestConstants.formatDate(new Date()), "provider", "FORMAT",
            content, 1L, 20, false, "red");
        channelTemplateManager.sendEvents(Arrays.asList(hipChatEvent));
    }

    @Test
    public void testNotAbstractChannelEvent() {
        final MockAuditEntryEntity mockAuditEntryEntity = new MockAuditEntryEntity();
        final AuditEntryRepository auditEntryRepositoryWrapper = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepositoryWrapper.save(Mockito.any(AuditEntryEntity.class))).thenReturn(mockAuditEntryEntity.createEntity());
        final AuditNotificationRepository auditNotificationRepositoryWrapper = Mockito.mock(AuditNotificationRepository.class);
        final JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
        Mockito.doNothing().when(jmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        final ChannelTemplateManager channelTemplateManager = new ChannelTemplateManager(gson, auditEntryRepositoryWrapper, auditNotificationRepositoryWrapper, jmsTemplate);
        final LinkableItem subTopic = new LinkableItem("subTopic", "sub topic", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "topic", null, subTopic, Collections.emptyList());
        final AlertEvent dbStoreEvent = new ContentEvent("", RestConstants.formatDate(new Date()), "", "FORMAT", content);
        final boolean isTrue = channelTemplateManager.sendEvent(dbStoreEvent);
        assertTrue(isTrue);
    }
}
