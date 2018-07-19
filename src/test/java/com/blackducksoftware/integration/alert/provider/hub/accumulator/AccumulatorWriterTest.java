package com.blackducksoftware.integration.alert.provider.hub.accumulator;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;

import com.blackducksoftware.integration.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.enumeration.InternalEventTypes;
import com.blackducksoftware.integration.alert.common.event.AlertEvent;
import com.blackducksoftware.integration.alert.common.model.NotificationModel;
import com.blackducksoftware.integration.alert.common.model.NotificationModels;
import com.blackducksoftware.integration.alert.workflow.NotificationManager;
import com.google.gson.Gson;

public class AccumulatorWriterTest {

    @Test
    public void testWrite() throws Exception {
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final Gson gson = new Gson();
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());
        final HubAccumulatorWriter hubAccumulatorWriter = new HubAccumulatorWriter(notificationManager, channelTemplateManager, contentConverter);

        final NotificationModel model = new NotificationModel(null, null);
        final NotificationModels models = new NotificationModels(Arrays.asList(model));
        final AlertEvent storeEvent = new AlertEvent(InternalEventTypes.DB_STORE_EVENT.getDestination(), contentConverter.getStringValue(models));
        hubAccumulatorWriter.write(Arrays.asList(storeEvent));

        Mockito.verify(channelTemplateManager).sendEvent(Mockito.any());
    }

    @Test
    public void testWriteNullData() throws Exception {
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final Gson gson = new Gson();
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());
        final HubAccumulatorWriter hubAccumulatorWriter = new HubAccumulatorWriter(notificationManager, channelTemplateManager, contentConverter);

        final NotificationModel model = new NotificationModel(null, null);
        final NotificationModels models = new NotificationModels(Arrays.asList(model));
        final AlertEvent storeEvent = new AlertEvent(InternalEventTypes.DB_STORE_EVENT.getDestination(), contentConverter.getStringValue(models));
        hubAccumulatorWriter.write(Arrays.asList(storeEvent));

        Mockito.verify(channelTemplateManager).sendEvent(Mockito.any());
    }
}
