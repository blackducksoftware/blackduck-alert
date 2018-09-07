package com.synopsys.integration.alert.common.workflow.processor;

public class CollectorLookupTest {

    // FIXME fix these when we have determined how we will do lookup
    //    @Test
    //    public void testFindFormat() {
    //        final FormatType formatType = FormatType.DIGEST;
    //        final TopicCollector topicCollector = Mockito.mock(TopicCollector.class);
    //        Mockito.when(topicCollector.getFormat()).thenReturn(formatType);
    //        final CollectorLookup collectorLookup = new CollectorLookup(Arrays.asList(topicCollector));
    //        final Optional<TopicCollector> actualTopicCollector = collectorLookup.findCollector(formatType);
    //        assertTrue(actualTopicCollector.isPresent());
    //        assertEquals(topicCollector, actualTopicCollector.get());
    //    }
    //
    //    @Test
    //    public void testFormatNotFound() {
    //        final FormatType formatType = FormatType.DIGEST;
    //        final TopicCollector topicCollector = Mockito.mock(TopicCollector.class);
    //        Mockito.when(topicCollector.getFormat()).thenReturn(formatType);
    //        final CollectorLookup collectorLookup = new CollectorLookup(Arrays.asList(topicCollector));
    //        final Optional<TopicCollector> actualTopicCollector = collectorLookup.findCollector(FormatType.DEFAULT);
    //        assertFalse(actualTopicCollector.isPresent());
    //    }
}
