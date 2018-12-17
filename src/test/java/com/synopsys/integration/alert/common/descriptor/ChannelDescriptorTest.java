package com.synopsys.integration.alert.common.descriptor;

import org.junit.Assert;
import org.junit.Test;

public class ChannelDescriptorTest {

    @Test
    public void getterTest() {
        final String name = "channel";
        final String destinationName = "hipchat";
        final ChannelDescriptor channelDescriptor = new ChannelDescriptor(name, destinationName, null, null) {};

        Assert.assertEquals(name, channelDescriptor.getName());
        Assert.assertEquals(destinationName, channelDescriptor.getDestinationName());
    }
}
