package com.blackducksoftware.integration.alert.descriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.alert.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.alert.descriptor.DescriptorMap;
import com.blackducksoftware.integration.alert.descriptor.DescriptorType;
import com.blackducksoftware.integration.alert.descriptor.ProviderDescriptor;

public class DescriptorMapTest {

    @Test
    public void testInit() {
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

        final DescriptorMap descriptorMap = new DescriptorMap(Arrays.asList(channelDescriptor1, channelDescriptor2), Arrays.asList(providerDescriptor));

        assertEquals(3, descriptorMap.getDescriptorMap().size());
        assertEquals(2, descriptorMap.getChannelDescriptorMap().size());
        assertEquals(1, descriptorMap.getProviderDescriptorMap().size());

        assertNotNull(descriptorMap.getChannelDescriptor(channelDescriptor1Name));
        assertNotNull(descriptorMap.getDescriptor(providerDescriptorName));

        assertNull(descriptorMap.getChannelDescriptor(providerDescriptorName));
        assertNull(descriptorMap.getDescriptor("Random name"));
    }
}
