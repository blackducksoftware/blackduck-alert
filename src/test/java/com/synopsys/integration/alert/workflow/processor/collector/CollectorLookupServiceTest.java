package com.synopsys.integration.alert.workflow.processor.collector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.workflow.processor.CollectorLookup;
import com.synopsys.integration.alert.common.workflow.processor.TopicCollector;

public class CollectorLookupServiceTest {

    private CollectorLookupService lookupService;

    public void initLookupService(final TopicCollector collector) {
        final String providerName = "myProvider";
        final FormatType formatType = FormatType.DIGEST;

        final CollectorLookup collectorLookup = Mockito.mock(CollectorLookup.class);
        final Provider provider = Mockito.mock(Provider.class);
        final ProviderDescriptor providerDescriptor = Mockito.mock(ProviderDescriptor.class);

        Mockito.when(collectorLookup.findCollector(formatType)).thenReturn(Optional.of(collector));
        Mockito.when(provider.getCollectorLookup()).thenReturn(collectorLookup);
        Mockito.when(providerDescriptor.getProvider()).thenReturn(provider);
        Mockito.when(providerDescriptor.getName()).thenReturn(providerName);

        final List<ProviderDescriptor> descriptorList = Arrays.asList(providerDescriptor);

        lookupService = new CollectorLookupService(descriptorList);
    }

    @Test
    public void testFindCollectorLookup() {
        final String providerName = "myProvider";
        final FormatType formatType = FormatType.DIGEST;
        final TopicCollector collector = Mockito.mock(TopicCollector.class);
        initLookupService(collector);
        final Optional<TopicCollector> actualCollectorLookup = lookupService.findCollector(providerName, formatType);
        assertTrue(actualCollectorLookup.isPresent());
        assertEquals(collector, actualCollectorLookup.get());
    }

    @Test
    public void testProviderNotFound() {
        final String providerName = "badProvider";
        final FormatType formatType = FormatType.DIGEST;
        final TopicCollector collector = Mockito.mock(TopicCollector.class);
        initLookupService(collector);
        final Optional<TopicCollector> actualCollectorLookup = lookupService.findCollector(providerName, formatType);
        assertFalse(actualCollectorLookup.isPresent());
    }

    @Test
    public void testFormatNotFound() {
        final String providerName = "myProvider";
        final FormatType formatType = FormatType.DEFAULT;
        final TopicCollector collector = Mockito.mock(TopicCollector.class);
        initLookupService(collector);
        final Optional<TopicCollector> actualCollectorLookup = lookupService.findCollector(providerName, formatType);
        assertFalse(actualCollectorLookup.isPresent());
    }
}
