package com.synopsys.integration.alert.common.descriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertException;

public class DescriptorMapTest {

    @Test
    public void testInit() throws AlertException {
        final ChannelDescriptor channelDescriptor1 = Mockito.mock(ChannelDescriptor.class);
        final ChannelDescriptor channelDescriptor2 = Mockito.mock(ChannelDescriptor.class);
        final ProviderDescriptor providerDescriptor = Mockito.mock(ProviderDescriptor.class);

        final String channelDescriptor1Name = "channelDescriptor1";
        final String channelDescriptor2Name = "channelDescriptor2";
        final String providerDescriptorName = "providerDescriptor";

        Mockito.when(channelDescriptor1.getName()).thenReturn(channelDescriptor1Name);
        Mockito.when(channelDescriptor2.getName()).thenReturn(channelDescriptor2Name);
        Mockito.when(providerDescriptor.getName()).thenReturn(providerDescriptorName);

        Mockito.when(channelDescriptor1.getType()).thenReturn(DescriptorType.CHANNEL);
        Mockito.when(channelDescriptor2.getType()).thenReturn(DescriptorType.CHANNEL);
        Mockito.when(providerDescriptor.getType()).thenReturn(DescriptorType.PROVIDER);

        final DescriptorMap descriptorMap = new DescriptorMap(Arrays.asList(channelDescriptor1, channelDescriptor2), Arrays.asList(providerDescriptor), Arrays.asList());

        assertEquals(3, descriptorMap.getDescriptorMap().size());
        assertEquals(2, descriptorMap.getChannelDescriptorMap().size());
        assertEquals(1, descriptorMap.getProviderDescriptorMap().size());

        assertNotNull(descriptorMap.getChannelDescriptor(channelDescriptor1Name));
        assertNotNull(descriptorMap.getDescriptor(providerDescriptorName));

        assertTrue(descriptorMap.getChannelDescriptor(providerDescriptorName).isEmpty());
        assertTrue(descriptorMap.getDescriptor("Random name").isEmpty());
    }
}
