package com.blackducksoftware.integration.hub.notification.accumulator.batch;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import com.blackducksoftware.integration.hub.notification.event.DBStoreEvent;

public class AccumulatorWriter implements ItemWriter<DBStoreEvent> {
    private final static Logger logger = LoggerFactory.getLogger(AccumulatorWriter.class);

    @Override
    public void write(final List<? extends DBStoreEvent> itemList) throws Exception {
        itemList.forEach(item -> {
            logger.info("Writing items: {} {}", item.getNotificationList());
        });
    }
}
