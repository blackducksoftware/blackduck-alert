package com.blackducksoftware.integration.hub.alert.accumulator;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.dataservice.notification.NotificationResults;

public class AccumulatorReaderTestIT {

    @Test
    public void testRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        final AccumulatorReader accumulatorReader = new AccumulatorReader(globalProperties);

        final NotificationResults actualNotificationResults = accumulatorReader.read();

        assertNotNull(actualNotificationResults);
    }
}
