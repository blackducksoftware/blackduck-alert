package com.blackducksoftware.integration.alert.provider.blackduck;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;

public class BlackDuckProviderTest {

    @Test
    public void testInitialize() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask);
        provider.initialize();
        Mockito.verify(accumulatorTask).scheduleExecution(BlackDuckAccumulator.DEFAULT_CRON_EXPRESSION);
    }

    @Test
    public void testDestroy() {
        final BlackDuckAccumulator accumulatorTask = Mockito.mock(BlackDuckAccumulator.class);
        final BlackDuckProvider provider = new BlackDuckProvider(accumulatorTask);
        provider.destroy();
        Mockito.verify(accumulatorTask).scheduleExecution(BlackDuckAccumulator.STOP_SCHEDULE_EXPRESSION);
    }
}
