package com.blackducksoftware.integration.alert.common.accumulator;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import com.blackducksoftware.integration.alert.common.exception.AlertException;
import com.blackducksoftware.integration.alert.workflow.scheduled.ScheduledTask;

public abstract class PollingAccumulator extends ScheduledTask implements Accumulator {

    public static final String ENCODING = "UTF-8";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String name;
    private final File searchRangeFilePath;

    public PollingAccumulator(final TaskScheduler taskScheduler, final String name, final File searchRangeFilePath) {
        super(taskScheduler);
        this.name = name;
        this.searchRangeFilePath = searchRangeFilePath;
    }

    @Override
    public void run() {
        accumulate();
    }

    @Override
    public void accumulate() {
        try {
            final Pair<Date, Date> dateRange = createDateRange(getSearchRangeFilePath());
            final String nextSearchStartTime = accumulate(dateRange);
            FileUtils.write(getSearchRangeFilePath(), nextSearchStartTime, ENCODING);
        } catch (final IOException | AlertException ex) {
            logger.error("Error occurred accumulating data! ", ex);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public File getSearchRangeFilePath() {
        return searchRangeFilePath;
    }

    protected abstract Pair<Date, Date> createDateRange(final File lastRunFile) throws AlertException;

    protected abstract String accumulate(Pair<Date, Date> dateRange) throws AlertException;

}

