package com.blackduck.integration.alert.api.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKey;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
class DistributionEventReceiverTest {
    private static final ChannelKey CHANNEL_KEY = new ChannelKey("test universal key", "Display Name");

    @Test
    void getDestinationTest() {
        DistributionEventReceiver<DistributionJobDetailsModel> receiver = new DistributionEventReceiver<>(null, null, CHANNEL_KEY, null) {};

        String destinationName = receiver.getDestinationName();
        assertEquals(CHANNEL_KEY.getUniversalKey(), destinationName);
    }

}
