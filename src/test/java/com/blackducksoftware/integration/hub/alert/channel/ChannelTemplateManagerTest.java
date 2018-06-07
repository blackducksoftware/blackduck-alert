package com.blackducksoftware.integration.hub.alert.channel;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.jms.core.JmsTemplate;

import com.blackducksoftware.integration.hub.alert.audit.mock.MockAuditEntryEntity;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryEntity;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditNotificationRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.event.AlertEvent;
import com.blackducksoftware.integration.hub.alert.event.AlertEventContentConverter;
import com.blackducksoftware.integration.hub.alert.event.ChannelEvent;
import com.google.gson.Gson;

public class ChannelTemplateManagerTest {
    private Gson gson;
    private AlertEventContentConverter contentConverter;

    @Before
    public void init() {
        gson = new Gson();
        contentConverter = new AlertEventContentConverter(gson);
    }

    @Test
    public void testSendEvents() {
        final MockAuditEntryEntity mockAuditEntryEntity = new MockAuditEntryEntity();
        final AuditEntryRepositoryWrapper auditEntryRepositoryWrapper = Mockito.mock(AuditEntryRepositoryWrapper.class);
        Mockito.when(auditEntryRepositoryWrapper.save(Mockito.any(AuditEntryEntity.class))).thenReturn(mockAuditEntryEntity.createEntity());
        final AuditNotificationRepositoryWrapper auditNotificationRepositoryWrapper = Mockito.mock(AuditNotificationRepositoryWrapper.class);
        final JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
        Mockito.doNothing().when(jmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        final ChannelTemplateManager channelTemplateManager = new ChannelTemplateManager(gson, auditEntryRepositoryWrapper, auditNotificationRepositoryWrapper, jmsTemplate, contentConverter);

        final ProjectData projectData = new ProjectData(DigestTypeEnum.DAILY, "test", "version", Arrays.asList(), null);
        final DigestModel digestModel = new DigestModel(Arrays.asList(projectData));
        final ChannelEvent hipChatEvent = new ChannelEvent(SupportedChannels.HIPCHAT, contentConverter.convertToString(digestModel), 1L);
        channelTemplateManager.sendEvents(Arrays.asList(hipChatEvent));
    }

    @Test
    public void testSendEventReturnsFalse() {
        final MockAuditEntryEntity mockAuditEntryEntity = new MockAuditEntryEntity();
        final AuditEntryRepositoryWrapper auditEntryRepositoryWrapper = Mockito.mock(AuditEntryRepositoryWrapper.class);
        Mockito.when(auditEntryRepositoryWrapper.save(Mockito.any(AuditEntryEntity.class))).thenReturn(mockAuditEntryEntity.createEntity());
        final AuditNotificationRepositoryWrapper auditNotificationRepositoryWrapper = Mockito.mock(AuditNotificationRepositoryWrapper.class);
        final JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
        Mockito.doNothing().when(jmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        final ChannelTemplateManager channelTemplateManager = new ChannelTemplateManager(gson, auditEntryRepositoryWrapper, auditNotificationRepositoryWrapper, jmsTemplate, contentConverter);

        final ChannelEvent slackEvent = new ChannelEvent(SupportedChannels.SLACK, null, 1L);
        final boolean isFalse = channelTemplateManager.sendEvent(slackEvent);
        assertTrue(!isFalse);
    }

    @Test
    public void testNotAbstractChannelEvent() {
        final MockAuditEntryEntity mockAuditEntryEntity = new MockAuditEntryEntity();
        final AuditEntryRepositoryWrapper auditEntryRepositoryWrapper = Mockito.mock(AuditEntryRepositoryWrapper.class);
        Mockito.when(auditEntryRepositoryWrapper.save(Mockito.any(AuditEntryEntity.class))).thenReturn(mockAuditEntryEntity.createEntity());
        final AuditNotificationRepositoryWrapper auditNotificationRepositoryWrapper = Mockito.mock(AuditNotificationRepositoryWrapper.class);
        final JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
        Mockito.doNothing().when(jmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        final ChannelTemplateManager channelTemplateManager = new ChannelTemplateManager(gson, auditEntryRepositoryWrapper, auditNotificationRepositoryWrapper, jmsTemplate, contentConverter);

        final AlertEvent dbStoreEvent = new AlertEvent("", null);
        final boolean isTrue = channelTemplateManager.sendEvent(dbStoreEvent);
        assertTrue(isTrue);
    }
}
