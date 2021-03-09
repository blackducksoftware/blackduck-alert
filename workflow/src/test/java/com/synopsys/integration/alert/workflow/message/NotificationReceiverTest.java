package com.synopsys.integration.alert.workflow.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.event.NotificationReceivedEvent;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.processor.api.NotificationProcessorV2;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractionDelegator;
import com.synopsys.integration.alert.processor.api.filter.JobNotificationMapper;
import com.synopsys.integration.alert.workflow.message.mocks.MockNotificationAccessor;
import com.synopsys.integration.rest.RestConstants;

public class NotificationReceiverTest {
    private NotificationAccessor notificationAccessor;
    private final Gson gson = new Gson();

    @Test
    public void handleEventTest() {
        AlertNotificationModel alertNotificationModel = createAlertNotificationModel(1L, false);
        List<AlertNotificationModel> alertNotificationModels = List.of(alertNotificationModel);

        NotificationProcessorV2 notificationProcessor = mockNotificationProcessor(alertNotificationModels);
        NotificationReceiverV2 notificationReceiver = new NotificationReceiverV2(gson, notificationAccessor, notificationProcessor);

        try {
            notificationReceiver.handleEvent(new NotificationReceivedEvent());
        } catch (RuntimeException e) {
            fail("Unable to handle event", e);
        }
    }

    @Test
    public void getDestinationNameTest() {
        NotificationReceiverV2 notificationReceiver = new NotificationReceiverV2(gson, null, null);

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
                                             .applyTopic("testTopic", "topic")
                                             .applySubTopic(subTopic.getLabel(), subTopic.getValue())
                                             .build();
        DistributionJobModel emptyJob = DistributionJobModel.builder().build();
        return new DistributionEvent("destination", RestConstants.formatDate(new Date()), 1L, "FORMAT",
            MessageContentGroup.singleton(content), emptyJob, null);
    }

    private NotificationProcessorV2 mockNotificationProcessor(List<AlertNotificationModel> alertNotificationModels) {
        NotificationDetailExtractionDelegator detailExtractionDelegator = new NotificationDetailExtractionDelegator(List.of());
        JobNotificationMapper jobNotificationMapper = Mockito.mock(JobNotificationMapper.class);
        Mockito.when(jobNotificationMapper.mapJobsToNotifications(Mockito.anyList(), Mockito.anyCollection())).thenReturn(List.of());
        notificationAccessor = new MockNotificationAccessor(alertNotificationModels);
        return new NotificationProcessorV2(detailExtractionDelegator, jobNotificationMapper, null, null, null, null, List.of(), notificationAccessor);
    }

}
