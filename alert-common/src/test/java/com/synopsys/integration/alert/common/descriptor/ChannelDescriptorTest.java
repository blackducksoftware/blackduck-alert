package com.synopsys.integration.alert.common.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

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

        assertEquals(name, channelDescriptor.getDescriptorKey().getUniversalKey());
    }

}
