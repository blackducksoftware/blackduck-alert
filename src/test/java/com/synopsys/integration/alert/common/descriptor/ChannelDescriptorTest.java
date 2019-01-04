package com.synopsys.integration.alert.common.descriptor;

import java.util.Collection;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.database.api.configuration.DefinedFieldModel;

public class ChannelDescriptorTest {

    @Test
    public void getterTest() {
        final String name = "channel";
        final String destinationName = "hipchat";
        final ChannelDescriptor channelDescriptor = new ChannelDescriptor(name, destinationName, null, null) {
            @Override
            public Collection<DefinedFieldModel> getDefinedFields(final ConfigContextEnum context) {
                return null;
            }
        };

        Assert.assertEquals(name, channelDescriptor.getName());
        Assert.assertEquals(destinationName, channelDescriptor.getDestinationName());
    }
}
