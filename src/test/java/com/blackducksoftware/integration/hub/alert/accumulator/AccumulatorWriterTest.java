package com.blackducksoftware.integration.hub.alert.accumulator;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.NotificationManager;
import com.blackducksoftware.integration.hub.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.hub.alert.event.AlertEvent;
import com.blackducksoftware.integration.hub.alert.event.AlertEventContentConverter;
import com.blackducksoftware.integration.hub.alert.event.InternalEventTypes;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModels;
import com.google.gson.Gson;

public class AccumulatorWriterTest {

    @Test
    public void testWrite() throws Exception {
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final Gson gson = new Gson();
        final AlertEventContentConverter contentConverter = new AlertEventContentConverter(gson);
        final AccumulatorWriter accumulatorWriter = new AccumulatorWriter(notificationManager, channelTemplateManager, contentConverter);

        final NotificationModel model = new NotificationModel(null, null);
        final NotificationModels models = new NotificationModels(Arrays.asList(model));
        final AlertEvent storeEvent = new AlertEvent(InternalEventTypes.DB_STORE_EVENT.getDestination(), contentConverter.convertToString(models));
        accumulatorWriter.write(Arrays.asList(storeEvent));

        Mockito.verify(channelTemplateManager).sendEvent(Mockito.any());
    }

    @Test
    public void testWriteNullData() throws Exception {
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final Gson gson = new Gson();
        final AlertEventContentConverter contentConverter = new AlertEventContentConverter(gson);
        final AccumulatorWriter accumulatorWriter = new AccumulatorWriter(notificationManager, channelTemplateManager, contentConverter);

        final NotificationModel model = new NotificationModel(null, null);
        final NotificationModels models = new NotificationModels(Arrays.asList(model));
        final AlertEvent storeEvent = new AlertEvent(InternalEventTypes.DB_STORE_EVENT.getDestination(), contentConverter.convertToString(models));
        accumulatorWriter.write(Arrays.asList(storeEvent));

        Mockito.verify(channelTemplateManager).sendEvent(Mockito.any());
    }
}
