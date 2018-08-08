package com.blackducksoftware.integration.alert.channel;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.jms.core.JmsTemplate;

import com.blackducksoftware.integration.alert.audit.mock.MockAuditEntryEntity;
import com.blackducksoftware.integration.alert.channel.event.ChannelEvent;
import com.blackducksoftware.integration.alert.channel.hipchat.HipChatChannel;
import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.digest.model.DigestModel;
import com.blackducksoftware.integration.alert.common.digest.model.ProjectData;
import com.blackducksoftware.integration.alert.common.enumeration.DigestType;
import com.blackducksoftware.integration.alert.common.event.AlertEvent;
import com.blackducksoftware.integration.alert.database.audit.AuditEntryEntity;
import com.blackducksoftware.integration.alert.database.audit.AuditEntryRepository;
import com.blackducksoftware.integration.alert.database.audit.AuditNotificationRepository;
import com.blackducksoftware.integration.alert.database.entity.NotificationContent;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.google.gson.Gson;

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
        final ChannelTemplateManager channelTemplateManager = new ChannelTemplateManager(gson, auditEntryRepository, auditNotificationRepositoryWrapper, jmsTemplate, contentConverter);

        final ProjectData projectData = new ProjectData(DigestType.DAILY, "test", "version", Arrays.asList(), null);
        final DigestModel digestModel = new DigestModel(Arrays.asList(projectData));
        final NotificationContent notificationContent = new NotificationContent(new Date(), "provider", "notificationType", contentConverter.getJsonString(digestModel));
        final ChannelEvent hipChatEvent = new ChannelEvent(HipChatChannel.COMPONENT_NAME, RestConnection.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(),
                notificationContent.getContent(), 1L, 1L);
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
        final ChannelTemplateManager channelTemplateManager = new ChannelTemplateManager(gson, auditEntryRepositoryWrapper, auditNotificationRepositoryWrapper, jmsTemplate, contentConverter);

        final AlertEvent dbStoreEvent = new AlertEvent("", RestConnection.formatDate(new Date()), "", "", null, 1L);
        final boolean isTrue = channelTemplateManager.sendEvent(dbStoreEvent);
        assertTrue(isTrue);
    }
}
