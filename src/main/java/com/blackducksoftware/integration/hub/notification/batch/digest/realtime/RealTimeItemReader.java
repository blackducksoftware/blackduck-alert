package com.blackducksoftware.integration.hub.notification.batch.digest.realtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class RealTimeItemReader implements ItemReader<Object> {
    private final static Logger logger = LoggerFactory.getLogger(RealTimeItemReader.class);

    @Override
    public Object read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        logger.info("Real Time Item Reader Called");
        return null;
    }
}
