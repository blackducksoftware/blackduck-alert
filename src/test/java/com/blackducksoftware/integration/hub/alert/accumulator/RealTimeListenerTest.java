package com.blackducksoftware.integration.hub.alert.accumulator;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.hub.alert.digest.filter.NotificationEventManager;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectDataFactory;
import com.blackducksoftware.integration.hub.alert.event.RealTimeEvent;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockNotificationEntity;
import com.google.gson.Gson;

public class RealTimeListenerTest {

    @Test
    public void testReceiveMessage() {
        final Gson gson = new Gson();
        final MockNotificationEntity notificationEntity = new MockNotificationEntity();
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final ProjectDataFactory projectDataFactory = new ProjectDataFactory();
        final NotificationEventManager notificationEventManager = Mockito.mock(NotificationEventManager.class);
        final RealTimeListener realTimeListener = new RealTimeListener(gson, channelTemplateManager, projectDataFactory, notificationEventManager);

        final RealTimeEvent realTimeEvent = new RealTimeEvent(Arrays.asList(notificationEntity.createEntity()));
        final String realTimeEventString = gson.toJson(realTimeEvent);
        realTimeListener.receiveMessage(realTimeEventString);

        Mockito.doNothing().when(channelTemplateManager).sendEvents(Mockito.any());
        Mockito.verify(channelTemplateManager).sendEvents(Mockito.any());
    }
}
