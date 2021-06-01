package com.synopsys.integration.alert.common.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public class DescriptorMapTest {
    private static final String MISSING_EXPECTED_DESCRIPTOR = "Missing expected descriptor";

    @Test
    public void getDescriptorKeyTest() {
        String expectedKeyValue = "expected_key";
        MockDescriptorKey key1 = new MockDescriptorKey("bad key 01");
        MockDescriptorKey key2 = new MockDescriptorKey(expectedKeyValue);
        MockDescriptorKey key3 = new MockDescriptorKey("bad key z");
        MockDescriptorKey key4 = new MockDescriptorKey("bad key x");
        DescriptorMap descriptorMap = new DescriptorMap(List.of(key1, key2, key3, key4), List.of());

        Optional<DescriptorKey> optionalFoundKey = descriptorMap.getDescriptorKey(expectedKeyValue);
        assertTrue(optionalFoundKey.isPresent(), "Missing expected descriptor key");
        DescriptorKey foundKey = optionalFoundKey.get();
        assertEquals(expectedKeyValue, foundKey.getUniversalKey());

        Optional<DescriptorKey> optionalMissingKey = descriptorMap.getDescriptorKey("a key that shouldn't be in there");
        assertTrue(optionalMissingKey.isEmpty(), "Did not expect to find a descriptor key");
    }

    @Test
    public void getDescriptorTest() {
        Descriptor channelDescriptor1 = createMockDescriptor("channel 1", DescriptorType.CHANNEL);
        Descriptor channelDescriptor2 = createMockDescriptor("channel 2", DescriptorType.CHANNEL);
        Descriptor channelDescriptor3 = createMockDescriptor("channel 3", DescriptorType.CHANNEL);

        Descriptor componentDescriptor1 = createMockDescriptor("component 1", DescriptorType.COMPONENT);
        Descriptor componentDescriptor2 = createMockDescriptor("component 2", DescriptorType.COMPONENT);

        Descriptor providerDescriptor1 = createMockDescriptor("provider 1", DescriptorType.PROVIDER);
        Descriptor providerDescriptor2 = createMockDescriptor("provider 2", DescriptorType.PROVIDER);

        DescriptorMap descriptorMap = new DescriptorMap(
            List.of(),
            List.of(channelDescriptor3, providerDescriptor2, channelDescriptor1, componentDescriptor2, componentDescriptor1, providerDescriptor1, channelDescriptor2)
        );

        Optional<Descriptor> optionalChannelDescriptor = descriptorMap.getDescriptor(channelDescriptor1.getDescriptorKey());
        assertTrue(optionalChannelDescriptor.isPresent(), MISSING_EXPECTED_DESCRIPTOR);

        Optional<Descriptor> optionalComponentDescriptor = descriptorMap.getDescriptor(componentDescriptor1.getDescriptorKey());
        assertTrue(optionalComponentDescriptor.isPresent(), MISSING_EXPECTED_DESCRIPTOR);

        Optional<Descriptor> optionalProviderDescriptor = descriptorMap.getDescriptor(providerDescriptor1.getDescriptorKey());
        assertTrue(optionalProviderDescriptor.isPresent(), MISSING_EXPECTED_DESCRIPTOR);

        Optional<Descriptor> optionalMissingDescriptor = descriptorMap.getDescriptor(new MockDescriptorKey("this should produce Optional.empty()"));
        assertTrue(optionalMissingDescriptor.isEmpty(), "Did not expect to find a descriptor");
    }

    @Test
    public void getDescriptorByTypeTest() {
        Descriptor channelDescriptor1 = createMockDescriptor("channel 1", DescriptorType.CHANNEL);
        Descriptor channelDescriptor2 = createMockDescriptor("channel 2", DescriptorType.CHANNEL);
        Descriptor channelDescriptor3 = createMockDescriptor("channel 3", DescriptorType.CHANNEL);

        Descriptor componentDescriptor1 = createMockDescriptor("component 1", DescriptorType.COMPONENT);
        Descriptor componentDescriptor2 = createMockDescriptor("component 2", DescriptorType.COMPONENT);

        Descriptor providerDescriptor1 = createMockDescriptor("provider 1", DescriptorType.PROVIDER);
        Descriptor providerDescriptor2 = createMockDescriptor("provider 2", DescriptorType.PROVIDER);

        DescriptorMap descriptorMap = new DescriptorMap(
            List.of(),
            List.of(channelDescriptor3, providerDescriptor2, channelDescriptor1, componentDescriptor2, componentDescriptor1, providerDescriptor1, channelDescriptor2)
        );

        Set<Descriptor> channelDescriptors = descriptorMap.getDescriptorByType(DescriptorType.CHANNEL);
        assertEquals(3, channelDescriptors.size());
        assertContains(channelDescriptors, channelDescriptor1);
        assertContains(channelDescriptors, channelDescriptor2);
        assertContains(channelDescriptors, channelDescriptor3);

        Set<Descriptor> componentDescriptors = descriptorMap.getDescriptorByType(DescriptorType.COMPONENT);
        assertEquals(2, componentDescriptors.size());
        assertContains(componentDescriptors, componentDescriptor1);
        assertContains(componentDescriptors, componentDescriptor2);

        Set<Descriptor> providerDescriptors = descriptorMap.getDescriptorByType(DescriptorType.PROVIDER);
        assertEquals(2, providerDescriptors.size());
        assertContains(providerDescriptors, providerDescriptor1);
        assertContains(providerDescriptors, providerDescriptor2);
    }

    private Descriptor createMockDescriptor(String descriptorName, DescriptorType descriptorType) {
        Descriptor descriptor = Mockito.mock(Descriptor.class);
        DescriptorKey descriptorKey = new MockDescriptorKey(descriptorName);

        Mockito.when(descriptor.getDescriptorKey()).thenReturn(descriptorKey);
        Mockito.when(descriptor.getType()).thenReturn(descriptorType);

        return descriptor;
    }

    private void assertContains(Set<Descriptor> descriptors, Descriptor expectedDescriptor) {
        assertTrue(descriptors.contains(expectedDescriptor), String.format("Expected %s to contain %s", descriptors, expectedDescriptor));
    }

    private static class MockDescriptorKey extends DescriptorKey {
        public MockDescriptorKey(String key) {
            super(key, key);
        }

    }

}
