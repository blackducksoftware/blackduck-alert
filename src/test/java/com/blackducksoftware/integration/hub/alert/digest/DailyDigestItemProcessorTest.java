package com.blackducksoftware.integration.hub.alert.digest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;

public class DailyDigestItemProcessorTest {

    @Test
    public void testGetDigestType() {
        final DailyDigestItemProcessor dailyDigestItemProcessor = new DailyDigestItemProcessor(null);

        assertEquals(DigestTypeEnum.DAILY, dailyDigestItemProcessor.getDigestType());
    }

    @Test
    public void testProcess() throws Exception {
        final DigestNotificationProcessor digestNotificationProcessor = Mockito.mock(DigestNotificationProcessor.class);
        final AbstractChannelEvent channelEvent = Mockito.mock(AbstractChannelEvent.class);

        Mockito.when(digestNotificationProcessor.processNotifications(Mockito.any(), Mockito.any())).thenReturn(Arrays.asList(channelEvent));

        final DailyDigestItemProcessor dailyDigestItemProcessor = new DailyDigestItemProcessor(digestNotificationProcessor);

        final List<AbstractChannelEvent> nonEmptyList = dailyDigestItemProcessor.process(Arrays.asList());
        assertTrue(!nonEmptyList.isEmpty());

        Mockito.when(digestNotificationProcessor.processNotifications(Mockito.any(), Mockito.any())).thenReturn(Arrays.asList());

        final List<AbstractChannelEvent> emptyList = dailyDigestItemProcessor.process(Arrays.asList());
        assertNull(emptyList);
    }
}
