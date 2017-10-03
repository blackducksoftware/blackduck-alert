package com.blackducksoftware.integration.hub.notification.batch.digest.realtime;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

public class RealTimeItemWriter implements ItemWriter<Object> {
    private final static Logger logger = LoggerFactory.getLogger(RealTimeItemWriter.class);

    @Override
    public void write(final List<? extends Object> itemList) throws Exception {
        logger.info("Real Time Item Writer called");
        // write events to message queues
    }
}
