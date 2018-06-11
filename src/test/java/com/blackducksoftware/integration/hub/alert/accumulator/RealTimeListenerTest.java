package com.blackducksoftware.integration.hub.alert.accumulator;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.OutputLogger;
import com.blackducksoftware.integration.hub.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.hub.alert.digest.filter.NotificationEventManager;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectDataFactory;
import com.blackducksoftware.integration.hub.alert.event.RealTimeEvent;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockNotificationEntity;
import com.google.gson.Gson;

public class RealTimeListenerTest {

    @Test
    public void testReceiveMessage() {
        final Gson gson = new Gson();
        final MockNotificationEntity notificationEntity = new MockNotificationEntity();
        final NotificationModel model = new NotificationModel(notificationEntity.createEntity(), Collections.emptyList());
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final ProjectDataFactory projectDataFactory = Mockito.mock(ProjectDataFactory.class);
        final NotificationEventManager eventManager = Mockito.mock(NotificationEventManager.class);

        final RealTimeListener realTimeListener = new RealTimeListener(gson, channelTemplateManager, projectDataFactory, eventManager);

        final RealTimeEvent realTimeEvent = new RealTimeEvent(Arrays.asList(model));
        final String realTimeEventString = gson.toJson(realTimeEvent);
        realTimeListener.receiveMessage(realTimeEventString);

        Mockito.doNothing().when(channelTemplateManager).sendEvents(Mockito.any());
        Mockito.verify(channelTemplateManager).sendEvents(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testReceiveMessageException() throws IOException, Exception {
        try (final OutputLogger outputLogger = new OutputLogger()) {
            final Gson gson = new Gson();
            final MockNotificationEntity notificationEntity = new MockNotificationEntity();
            final NotificationModel model = new NotificationModel(notificationEntity.createEntity(), Collections.emptyList());
            final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
            final ProjectDataFactory projectDataFactory = Mockito.mock(ProjectDataFactory.class);
            final NotificationEventManager eventManager = Mockito.mock(NotificationEventManager.class);

            Mockito.doNothing().when(channelTemplateManager).sendEvents(Mockito.any());
            Mockito.doThrow(new NullPointerException("null error")).when(eventManager).createChannelEvents(Mockito.any(), Mockito.anyList());

            final RealTimeListener realTimeListener = new RealTimeListener(gson, channelTemplateManager, projectDataFactory, eventManager);

            final RealTimeEvent realTimeEvent = new RealTimeEvent(Arrays.asList(model));
            final String realTimeEventString = gson.toJson(realTimeEvent);
            realTimeListener.receiveMessage(realTimeEventString);

            assertTrue(outputLogger.isLineContainingText("null"));
        }

    }
}
