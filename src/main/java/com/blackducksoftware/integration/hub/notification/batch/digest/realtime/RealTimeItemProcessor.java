package com.blackducksoftware.integration.hub.notification.batch.digest.realtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class RealTimeItemProcessor implements ItemProcessor<Object, Object> {
    private final static Logger logger = LoggerFactory.getLogger(RealTimeItemReader.class);

    @Override
    public Object process(final Object notificationData) throws Exception {
        logger.info("Real Time Item Processor Called");
        return null;
    }
}
