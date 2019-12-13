package com.synopsys.integration.alert.common.descriptor;

import java.util.Set;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.channel.key.ChannelKey;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;

public class ChannelDescriptorTest {

    @Test
    public void getterTest() {
        String name = "channel";
        ChannelKey channelKey = new ChannelKey() {
            @Override
            public String getUniversalKey() {
                return name;
            }

            @Override
            public String getDisplayName() {
                return name;
            }
        };
        ChannelDescriptor channelDescriptor = new ChannelDescriptor(channelKey, null, null) {
            @Override
            public Set<DefinedFieldModel> getAllDefinedFields(ConfigContextEnum context) {
                return null;
            }
        };

        Assert.assertEquals(name, channelDescriptor.getDescriptorKey().getUniversalKey());
    }

}
