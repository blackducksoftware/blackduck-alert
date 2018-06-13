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
import com.blackducksoftware.integration.hub.alert.event.AlertEvent;
import com.blackducksoftware.integration.hub.alert.event.AlertEventContentConverter;
import com.blackducksoftware.integration.hub.alert.event.InternalEventTypes;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModels;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockNotificationEntity;
import com.google.gson.Gson;

public class RealTimeListenerTest {

    @Test
    public void testReceiveMessage() {
        final Gson gson = new Gson();
        final MockNotificationEntity notificationEntity = new MockNotificationEntity();
        final NotificationModel model = new NotificationModel(notificationEntity.createEntity(), Collections.emptyList());
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final NotificationEventManager eventManager = Mockito.mock(NotificationEventManager.class);
        final AlertEventContentConverter contentConverter = new AlertEventContentConverter(gson);

        Mockito.doNothing().when(channelTemplateManager).sendEvents(Mockito.any());
        final RealTimeListener realTimeListener = new RealTimeListener(gson, channelTemplateManager, eventManager, contentConverter);

        final AlertEvent realTimeEvent = new AlertEvent(InternalEventTypes.REAL_TIME_EVENT.getDestination(), contentConverter.convertToString(Arrays.asList(model)));
        realTimeListener.handleEvent(realTimeEvent);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testReceiveMessageException() throws IOException, Exception {
        try (final OutputLogger outputLogger = new OutputLogger()) {
            final Gson gson = new Gson();
            final MockNotificationEntity notificationEntity = new MockNotificationEntity();
            final NotificationModel notificationModel = new NotificationModel(notificationEntity.createEntity(), Collections.emptyList());
            final NotificationModels model = new NotificationModels(Arrays.asList(notificationModel));
            final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
            final NotificationEventManager eventManager = Mockito.mock(NotificationEventManager.class);
            final AlertEventContentConverter contentConverter = new AlertEventContentConverter(gson);

            Mockito.doNothing().when(channelTemplateManager).sendEvents(Mockito.any());
            Mockito.doThrow(new NullPointerException("null error")).when(eventManager).createChannelEvents(Mockito.any(), Mockito.anyList());

            final RealTimeListener realTimeListener = new RealTimeListener(gson, channelTemplateManager, eventManager, contentConverter);

            final AlertEvent realTimeEvent = new AlertEvent(InternalEventTypes.REAL_TIME_EVENT.getDestination(), contentConverter.convertToString(model));
            realTimeListener.handleEvent(realTimeEvent);

            assertTrue(outputLogger.isLineContainingText("null"));
        }

    }
}
