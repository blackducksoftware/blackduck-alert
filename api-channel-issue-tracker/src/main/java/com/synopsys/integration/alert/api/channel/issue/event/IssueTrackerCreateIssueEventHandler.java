/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.api.event.AlertEventHandler;

public class IssueTrackerCreateIssueEventHandler implements AlertEventHandler<IssueTrackerCreateIssueEvent> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void handle(IssueTrackerCreateIssueEvent event) {
        logger.info("issue tracker create event handler called.");

    }
}
