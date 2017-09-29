package com.blackducksoftware.integration.hub.notification.accumulator.batch;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import com.blackducksoftware.integration.hub.dataservice.notification.NotificationResults;

public class AccumulatorWriter implements ItemWriter<NotificationResults> {
    private final static Logger logger = LoggerFactory.getLogger(AccumulatorWriter.class);

    @Override
    public void write(final List<? extends NotificationResults> itemList) throws Exception {
        itemList.forEach(item -> {
            logger.info("Writing item {}", item);
        });
    }

}
