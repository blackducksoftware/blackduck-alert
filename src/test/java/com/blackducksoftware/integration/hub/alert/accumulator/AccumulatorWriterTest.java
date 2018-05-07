package com.blackducksoftware.integration.hub.alert.accumulator;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.NotificationManager;
import com.blackducksoftware.integration.hub.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.hub.alert.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;

public class AccumulatorWriterTest {

    @Test
    public void testWrite() throws Exception {
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);

        final AccumulatorWriter accumulatorWriter = new AccumulatorWriter(notificationManager, channelTemplateManager);

        final NotificationModel model = new NotificationModel(null, null);
        final DBStoreEvent storeEvent = new DBStoreEvent(Arrays.asList(model));

        accumulatorWriter.write(Arrays.asList(storeEvent));

        Mockito.verify(channelTemplateManager).sendEvent(Mockito.any());
    }

    @Test
    public void testWriteNullData() throws Exception {
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);

        final AccumulatorWriter accumulatorWriter = new AccumulatorWriter(notificationManager, channelTemplateManager);

        final NotificationModel model = new NotificationModel(null, null);
        final DBStoreEvent storeEvent = new DBStoreEvent(Arrays.asList(model));

        accumulatorWriter.write(Arrays.asList(storeEvent));

        Mockito.verify(channelTemplateManager).sendEvent(Mockito.any());
    }
}
