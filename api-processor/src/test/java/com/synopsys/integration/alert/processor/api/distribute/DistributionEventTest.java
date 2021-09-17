package com.synopsys.integration.alert.processor.api.distribute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.descriptor.api.SlackChannelKey;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;

public class DistributionEventTest {
    private final static UUID JOB_ID = UUID.randomUUID();
    private final static String JOB_NAME = "jobName";
    private final static Long NOTIFICATION_ID = 1L;

    private final SlackChannelKey channelKey = new SlackChannelKey();
    private final ProviderMessageHolder providerMessageHolder = ProviderMessageHolder.empty();

    @Test
    public void getJobIdTest() {
        DistributionEvent event = new DistributionEvent(channelKey, JOB_ID, JOB_NAME, Set.of(NOTIFICATION_ID), providerMessageHolder);
        assertEquals(JOB_ID, event.getJobId());
    }

    @Test
    public void getJobNameTest() {
        DistributionEvent event = new DistributionEvent(channelKey, JOB_ID, JOB_NAME, Set.of(NOTIFICATION_ID), providerMessageHolder);
        assertEquals(JOB_NAME, event.getJobName());
    }

    @Test
    public void getNotificationIdsTest() {
        DistributionEvent event = new DistributionEvent(channelKey, JOB_ID, JOB_NAME, Set.of(NOTIFICATION_ID), providerMessageHolder);
        assertTrue(event.getNotificationIds().contains(NOTIFICATION_ID));
    }

    @Test
    public void getProviderMessagesTest() {
        DistributionEvent event = new DistributionEvent(channelKey, JOB_ID, JOB_NAME, Set.of(NOTIFICATION_ID), providerMessageHolder);
        assertEquals(providerMessageHolder, event.getProviderMessages());
    }
}
