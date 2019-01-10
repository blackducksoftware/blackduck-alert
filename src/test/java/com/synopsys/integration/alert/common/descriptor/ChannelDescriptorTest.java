package com.synopsys.integration.alert.common.descriptor;

import java.util.Set;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.database.api.configuration.model.DefinedFieldModel;

public class ChannelDescriptorTest {

    @Test
    public void getterTest() {
        final String name = "channel";
        final String destinationName = "hipchat";
        final ChannelDescriptor channelDescriptor = new ChannelDescriptor(name, destinationName, null, null) {
            @Override
            public Set<DefinedFieldModel> getAllDefinedFields(final ConfigContextEnum context) {
                return null;
            }
        };

        Assert.assertEquals(name, channelDescriptor.getName());
        Assert.assertEquals(destinationName, channelDescriptor.getDestinationName());
    }
}
