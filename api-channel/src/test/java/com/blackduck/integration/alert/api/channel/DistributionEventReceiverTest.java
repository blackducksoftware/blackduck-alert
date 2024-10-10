/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKey;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
class DistributionEventReceiverTest {
    private static final ChannelKey CHANNEL_KEY = new ChannelKey("test universal key", "Display Name");

    @Test
    void getDestinationTest() {
        DistributionEventReceiver<DistributionJobDetailsModel> receiver = new DistributionEventReceiver<>(null, null, CHANNEL_KEY, null) {};

        String destinationName = receiver.getDestinationName();
        assertEquals(CHANNEL_KEY.getUniversalKey(), destinationName);
    }

}
