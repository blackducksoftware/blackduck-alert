package com.synopsys.integration.alert.common.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public class DescriptorMapTest {
    @Test
    public void testInit() throws AlertException {
        ChannelDescriptor channelDescriptor1 = Mockito.mock(ChannelDescriptor.class);
        ChannelDescriptor channelDescriptor2 = Mockito.mock(ChannelDescriptor.class);

        final String channelDescriptor1Name = "channelDescriptor1";
        final String channelDescriptor2Name = "channelDescriptor2";

        DescriptorKey channelDescriptor1Key = new MockDescriptorKey(channelDescriptor1Name);
        DescriptorKey channelDescriptor2Key = new MockDescriptorKey(channelDescriptor2Name);

        Mockito.when(channelDescriptor1.getDescriptorKey()).thenReturn(channelDescriptor1Key);
        Mockito.when(channelDescriptor2.getDescriptorKey()).thenReturn(channelDescriptor2Key);

        Mockito.when(channelDescriptor1.getType()).thenReturn(DescriptorType.CHANNEL);
        Mockito.when(channelDescriptor2.getType()).thenReturn(DescriptorType.CHANNEL);

        DescriptorMap descriptorMap = new DescriptorMap(List.of(channelDescriptor1Key, channelDescriptor2Key), List.of(channelDescriptor1, channelDescriptor2));

        assertEquals(2, descriptorMap.getDescriptorMap().size());
        assertEquals(2, descriptorMap.getChannelDescriptorMap().size());

        assertNotNull(descriptorMap.getChannelDescriptor(channelDescriptor1Key));

        DescriptorKey randomKey = new MockDescriptorKey("Random name");
        assertTrue(descriptorMap.getDescriptor(randomKey).isEmpty());

        assertTrue(descriptorMap.getDescriptorKey("Random name").isEmpty());
    }

    private class MockDescriptorKey extends DescriptorKey {

        public MockDescriptorKey(String key) {
            super(key, key);
        }

    }

}
