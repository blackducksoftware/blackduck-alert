package com.blackducksoftware.integration.hub.alert.accumulator;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Test;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.blackducksoftware.integration.hub.alert.OutputLogger;
import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.dataservice.notification.NotificationResults;

public class AccumulatorReaderTestIT {
    private final OutputLogger outputLogger;

    public AccumulatorReaderTestIT() throws IOException {
        outputLogger = new OutputLogger();
    }

    @After
    public void cleanup() throws IOException {
        outputLogger.cleanup();
    }

    @Test
    public void testRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        final AccumulatorReader accumulatorReader = new AccumulatorReader(globalProperties);

        final NotificationResults actualNotificationResults = accumulatorReader.read();

        assertNull(actualNotificationResults);
        assertTrue(outputLogger.isLineContainingText("Read Notification Count: 0"));
    }

}
