/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IssueTrackerChannelLock {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // TODO: make this configurable per channel.
    // 30 minutes
    public static final int DEFAULT_TIMEOUT_SECONDS = 1800;
    private static final int PERMITS = 1;
    private String lockName;
    private Semaphore lock;

    public IssueTrackerChannelLock(String lockName) {
        this.lockName = lockName;
        this.lock = new Semaphore(PERMITS, true);
    }

    public boolean getLock(int timeoutSeconds) {
        boolean acquired = false;
        try {
            acquired = lock.tryAcquire(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            logger.error("Interrupted Exception acquiring {} lock.", lockName);
            logger.error("Cause: ", ex);
            Thread.currentThread().interrupt();
        } finally {
            if (acquired) {
                logger.info("Acquired {} lock.", lockName);
            } else {
                logger.error("Could not acquire {} lock.", lockName);
            }
        }
        return acquired;
    }

    public void release() {
        logger.info("Releasing {} lock.", lockName);
        this.lock.release();
    }
}
