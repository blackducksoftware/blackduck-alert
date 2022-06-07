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
    private static final int PERMITS = 1;
    private String lockName;
    private long timeoutSeconds;
    private Semaphore lock;

    public IssueTrackerChannelLock(String lockName, long timeoutSeconds) {
        this.lockName = lockName;
        this.timeoutSeconds = timeoutSeconds;
        this.lock = new Semaphore(PERMITS, true);
    }

    public boolean getLock() {
        boolean acquired = false;
        try {
            acquired = this.lock.tryAcquire(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (!acquired) {
                logger.error("Could not acquire {} lock.", lockName);
            }
        }
        return acquired;
    }

    public void release() {
        this.lock.release();
    }
}
