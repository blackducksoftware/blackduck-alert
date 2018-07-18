package com.blackducksoftware.integration.alert.digest;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.alert.common.enumeration.DigestType;
import com.blackducksoftware.integration.alert.event.ChannelEvent;

public class DailyDigestItemProcessorTest {

    @Test
    public void testGetDigestType() {
        final DailyDigestItemProcessor dailyDigestItemProcessor = new DailyDigestItemProcessor(null);

        assertEquals(DigestType.DAILY, dailyDigestItemProcessor.getDigestType());
    }

    @Test
    public void testProcess() throws Exception {
        final DigestNotificationProcessor digestNotificationProcessor = Mockito.mock(DigestNotificationProcessor.class);
        final ChannelEvent channelEvent = Mockito.mock(ChannelEvent.class);

        Mockito.when(digestNotificationProcessor.processNotifications(Mockito.any(), Mockito.any())).thenReturn(Arrays.asList(channelEvent));

        final DailyDigestItemProcessor dailyDigestItemProcessor = new DailyDigestItemProcessor(digestNotificationProcessor);

        final List<ChannelEvent> nonEmptyList = dailyDigestItemProcessor.process(Arrays.asList());
        assertTrue(!nonEmptyList.isEmpty());

        Mockito.when(digestNotificationProcessor.processNotifications(Mockito.any(), Mockito.any())).thenReturn(Arrays.asList());

        final List<ChannelEvent> emptyList = dailyDigestItemProcessor.process(Arrays.asList());
        assertNull(emptyList);
    }

    @Test
    public void testProcessException() throws Exception {
        final DigestNotificationProcessor digestNotificationProcessor = Mockito.mock(DigestNotificationProcessor.class);

        Mockito.when(digestNotificationProcessor.processNotifications(Mockito.any(), Mockito.any())).thenThrow(new NullPointerException());

        final DailyDigestItemProcessor dailyDigestItemProcessor = new DailyDigestItemProcessor(digestNotificationProcessor);

        final List<ChannelEvent> nullList = dailyDigestItemProcessor.process(Arrays.asList());
        assertNull(nullList);
    }
}
