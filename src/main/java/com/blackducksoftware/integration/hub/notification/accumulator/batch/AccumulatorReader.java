package com.blackducksoftware.integration.hub.notification.accumulator.batch;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.blackducksoftware.integration.hub.dataservice.notification.NotificationDataService;
import com.blackducksoftware.integration.hub.dataservice.notification.NotificationResults;
import com.blackducksoftware.integration.hub.notification.HubServiceWrapper;
import com.blackducksoftware.integration.hub.rest.RestConnection;

public class AccumulatorReader implements ItemReader<NotificationResults> {
    private final static Logger logger = LoggerFactory.getLogger(AccumulatorReader.class);

    private final HubServiceWrapper hubServiceWrapper;
    private final String lastRunPath;

    public AccumulatorReader(final HubServiceWrapper hubServiceWrapper) {
        this.hubServiceWrapper = hubServiceWrapper;
        lastRunPath = findLastRunFilePath();
    }

    private String findLastRunFilePath() {
        String path = "";
        try {
            final String configLocation = System.getProperty("/");
            final File file = new File(configLocation, "accumulator-lastrun.txt");
            path = file.getCanonicalPath();
        } catch (final IOException ex) {
            logger.error("Cannot find last run file path", ex);
        }
        return path;
    }

    @Override
    public NotificationResults read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        logger.info("Accumulator Reader Called");
        final Date endDate = new Date();
        Date startDate = endDate;
        try {
            final File lastRunFile = new File(lastRunPath);
            if (lastRunFile.exists()) {
                final String lastRunValue = FileUtils.readFileToString(lastRunFile, "UTF-8");
                startDate = RestConnection.parseDateString(lastRunValue);
                startDate = new Date(startDate.getTime());
            } else {
                startDate = endDate;
            }
            FileUtils.write(lastRunFile, RestConnection.formatDate(endDate), "UTF-8");
        } catch (final Exception e) {
            logger.error("Error creating date range", e);
        }

        final NotificationDataService notificationDataService = hubServiceWrapper.getHubServicesFactory().createNotificationDataService();
        final NotificationResults notificationResults = notificationDataService.getAllNotifications(startDate, endDate);

        if (notificationResults.getNotificationContentItems().isEmpty()) {
            return null;
        }

        return notificationResults;
    }

}
