package com.synopsys.integration.alert.workflow.message;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.channel.ChannelEventManager;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.event.NotificationReceivedEvent;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.common.workflow.processor.notification.NotificationProcessor;
import com.synopsys.integration.alert.workflow.message.mocks.MockNotificationAccessor;
import com.synopsys.integration.rest.RestConstants;

public class NotificationReceiverTest {
    private NotificationAccessor notificationAccessor;
    private final Gson gson = new Gson();

    @Test
    public void handleEventTest() throws Exception {
        List<AlertNotificationModel> alertNotificationModels = List.of(createAlertNotificationModel(1L, false));
        DistributionEvent distributionEvent = createDistributionEvent(1L);

        NotificationProcessor notificationProcessor = Mockito.mock(NotificationProcessor.class);
        ChannelEventManager channelEventManager = Mockito.mock(ChannelEventManager.class);

        notificationAccessor = new MockNotificationAccessor(alertNotificationModels);

        Mockito.when(notificationProcessor.processNotifications(Mockito.eq(FrequencyType.REAL_TIME), Mockito.eq(alertNotificationModels))).thenReturn(List.of(distributionEvent));

        NotificationReceiver notificationReceiver = new NotificationReceiver(gson, notificationAccessor, notificationProcessor, channelEventManager);
        notificationReceiver.handleEvent(new NotificationReceivedEvent());

        Mockito.verify(channelEventManager, Mockito.times(1)).sendEvents(Mockito.any());
    }

    //TODO: Once NotificationReceiver is updated and the MAX_NUMBER_PAGES_PROCESSED is removed this will no longer need to be tested
    @Test
    public void handleEventMaxPagesProcessedTest() throws Exception {
        List<AlertNotificationModel> alertNotificationModels = List.of(createAlertNotificationModel(1L, false));
        DistributionEvent distributionEvent = createDistributionEvent(1L);

        Page<AlertNotificationModel> pageOfNotifications = new PageImpl<>(alertNotificationModels);

        NotificationAccessor notificationAccessorMock = Mockito.mock(NotificationAccessor.class);
        NotificationProcessor notificationProcessor = Mockito.mock(NotificationProcessor.class);
        ChannelEventManager channelEventManager = Mockito.mock(ChannelEventManager.class);

        Mockito.when(notificationProcessor.processNotifications(Mockito.eq(FrequencyType.REAL_TIME), Mockito.eq(alertNotificationModels))).thenReturn(List.of(distributionEvent));
        Mockito.when(notificationAccessorMock.getFirstPageOfNotificationsNotProcessed(100))
            .thenReturn(new AlertPagedModel(pageOfNotifications.getTotalPages(), pageOfNotifications.getNumber(), pageOfNotifications.getSize(), pageOfNotifications.getContent()));

        NotificationReceiver notificationReceiver = new NotificationReceiver(gson, notificationAccessorMock, notificationProcessor, channelEventManager);
        notificationReceiver.handleEvent(new NotificationReceivedEvent());

        Mockito.verify(channelEventManager, Mockito.times(100)).sendEvents(Mockito.any());
    }

    @Test
    public void getDestinationNameTest() {
        NotificationReceiver notificationReceiver = new NotificationReceiver(gson, null, null, null);

        assertEquals(NotificationReceivedEvent.NOTIFICATION_RECEIVED_EVENT_TYPE, notificationReceiver.getDestinationName());
    }

    private AlertNotificationModel createAlertNotificationModel(Long id, boolean processed) {
        Long providerConfigId = 2L;
        String provider = "provider-test";
        String notificationType = "notificationType-test";
        String content = "content";
        String providerConfigName = "providerConfigName";

        return new AlertNotificationModel(id, providerConfigId, provider, providerConfigName, notificationType, content, DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp(), processed);
    }

    private DistributionEvent createDistributionEvent(Long providerConfigId) throws Exception {
        LinkableItem subTopic = new LinkableItem("subTopic", "sub topic", null);
        ProviderMessageContent content = new ProviderMessageContent.Builder()
                                             .applyProvider("testProvider", providerConfigId, "testProviderConfig")
                                             .applyProject("testTopic", "topic")
                                             .applyProjectVersion(subTopic.getLabel(), subTopic.getValue())
                                             .build();
        DistributionJobModel emptyJob = DistributionJobModel.builder().build();
        return new DistributionEvent("destination", RestConstants.formatDate(new Date()), 1L, "FORMAT",
            MessageContentGroup.singleton(content), emptyJob, null);
    }

}
