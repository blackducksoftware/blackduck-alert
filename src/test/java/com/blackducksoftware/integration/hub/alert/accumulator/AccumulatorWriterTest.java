package com.blackducksoftware.integration.hub.alert.accumulator;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.NotificationManager;
import com.blackducksoftware.integration.hub.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.hub.alert.event.InternalEventTypes;
import com.blackducksoftware.integration.hub.alert.event.NotificationListEvent;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModels;

public class AccumulatorWriterTest {

    @Test
    public void testWrite() throws Exception {
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);

        final AccumulatorWriter accumulatorWriter = new AccumulatorWriter(notificationManager, channelTemplateManager);

        final NotificationModel model = new NotificationModel(null, null);
        final NotificationModels models = new NotificationModels(Arrays.asList(model));
        final NotificationListEvent storeEvent = new NotificationListEvent(InternalEventTypes.DB_STORE_EVENT.getDestination(), models);
        accumulatorWriter.write(Arrays.asList(storeEvent));

        Mockito.verify(channelTemplateManager).sendEvent(Mockito.any());
    }

    @Test
    public void testWriteNullData() throws Exception {
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);

        final AccumulatorWriter accumulatorWriter = new AccumulatorWriter(notificationManager, channelTemplateManager);

        final NotificationModel model = new NotificationModel(null, null);
        final NotificationModels models = new NotificationModels(Arrays.asList(model));
        final NotificationListEvent storeEvent = new NotificationListEvent(InternalEventTypes.DB_STORE_EVENT.getDestination(), models);
        accumulatorWriter.write(Arrays.asList(storeEvent));

        Mockito.verify(channelTemplateManager).sendEvent(Mockito.any());
    }
}
