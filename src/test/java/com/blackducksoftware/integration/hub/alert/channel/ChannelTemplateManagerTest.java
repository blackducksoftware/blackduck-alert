package com.blackducksoftware.integration.hub.alert.channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.AbstractJmsTemplate;
import com.blackducksoftware.integration.hub.alert.audit.mock.MockAuditEntryEntity;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryEntity;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatEvent;
import com.blackducksoftware.integration.hub.alert.channel.slack.SlackEvent;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.event.NotificationListEvent;
import com.google.gson.Gson;

public class ChannelTemplateManagerTest {
    protected static int testCount = 0;

    @Test
    public void testSendEvents() {
        final MockAuditEntryEntity mockAuditEntryEntity = new MockAuditEntryEntity();
        final AuditEntryRepositoryWrapper auditEntryRepositoryWrapper = Mockito.mock(AuditEntryRepositoryWrapper.class);
        Mockito.when(auditEntryRepositoryWrapper.save(Mockito.any(AuditEntryEntity.class))).thenReturn(mockAuditEntryEntity.createEntity());
        final ChannelTemplateManager channelTemplateManager = new ChannelTemplateManager(new Gson(), auditEntryRepositoryWrapper, null, null) {

            @Override
            public boolean hasTemplate(final String destination) {
                return true;
            }

            @Override
            public AbstractJmsTemplate getTemplate(final String destination) {
                testCount++;
                final AbstractJmsTemplate abstractJmsTemplate = Mockito.mock(AbstractJmsTemplate.class);
                Mockito.doNothing().when(abstractJmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
                return abstractJmsTemplate;
            }
        };

        testCount = 0;
        final ProjectData projectData = new ProjectData(DigestTypeEnum.DAILY, "test", "version", Arrays.asList(), null);
        final HipChatEvent slackEvent = new HipChatEvent(Arrays.asList(projectData), 1L);
        channelTemplateManager.sendEvents(Arrays.asList(slackEvent));

        assertEquals(1, testCount);
    }

    @Test
    public void testSendEventReturnsFalse() {
        final ChannelTemplateManager channelTemplateManager = new ChannelTemplateManager(new Gson(), null, null, null);

        final SlackEvent slackEvent = new SlackEvent(null, 1L);
        final boolean isFalse = channelTemplateManager.sendEvent(slackEvent);
        assertTrue(!isFalse);
    }

    @Test
    public void testNotAbstractChannelEvent() {
        final ChannelTemplateManager channelTemplateManager = new ChannelTemplateManager(new Gson(), null, null, null) {
            @Override
            public boolean hasTemplate(final String destination) {
                return true;
            }

            @Override
            public AbstractJmsTemplate getTemplate(final String destination) {
                final AbstractJmsTemplate abstractJmsTemplate = Mockito.mock(AbstractJmsTemplate.class);
                Mockito.doNothing().when(abstractJmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
                return abstractJmsTemplate;
            }
        };

        final NotificationListEvent dbStoreEvent = new NotificationListEvent("", null);
        final boolean isTrue = channelTemplateManager.sendEvent(dbStoreEvent);
        assertTrue(isTrue);
    }
}
