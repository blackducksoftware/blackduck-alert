package com.synopsys.integration.alert.api.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

public class DistributionEventReceiverTest {
    private static final ChannelKey CHANNEL_KEY = new ChannelKey("test universal key", "Display Name");

    @Test
    public void getDestinationTest() {
        DistributionEventReceiver<DistributionJobDetailsModel> receiver = new DistributionEventReceiver<>(null, CHANNEL_KEY, null) {};

        String destinationName = receiver.getDestinationName();
        assertEquals(CHANNEL_KEY.getUniversalKey(), destinationName);
    }

}
