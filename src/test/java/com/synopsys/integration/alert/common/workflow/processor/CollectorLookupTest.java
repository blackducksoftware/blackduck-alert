package com.synopsys.integration.alert.common.workflow.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.FormatType;

public class CollectorLookupTest {

    @Test
    public void testFindFormat() {
        final FormatType formatType = FormatType.DIGEST;
        final TopicCollector topicCollector = Mockito.mock(TopicCollector.class);
        Mockito.when(topicCollector.getFormat()).thenReturn(formatType);
        final CollectorLookup collectorLookup = new CollectorLookup(Arrays.asList(topicCollector));
        final Optional<TopicCollector> actualTopicCollector = collectorLookup.findCollector(formatType);
        assertTrue(actualTopicCollector.isPresent());
        assertEquals(topicCollector, actualTopicCollector.get());
    }

    @Test
    public void testFormatNotFound() {
        final FormatType formatType = FormatType.DIGEST;
        final TopicCollector topicCollector = Mockito.mock(TopicCollector.class);
        Mockito.when(topicCollector.getFormat()).thenReturn(formatType);
        final CollectorLookup collectorLookup = new CollectorLookup(Arrays.asList(topicCollector));
        final Optional<TopicCollector> actualTopicCollector = collectorLookup.findCollector(FormatType.DEFAULT);
        assertFalse(actualTopicCollector.isPresent());
    }
}
