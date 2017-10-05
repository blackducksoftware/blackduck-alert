package com.blackducksoftware.integration.hub.notification.batch.digest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

public class DigestItemWriter implements ItemWriter<Object> {
    private final static Logger logger = LoggerFactory.getLogger(DigestItemWriter.class);

    @Override
    public void write(final List<? extends Object> itemList) throws Exception {
        logger.info("Real Time Item Writer called");
        // write events to message queues
    }
}
