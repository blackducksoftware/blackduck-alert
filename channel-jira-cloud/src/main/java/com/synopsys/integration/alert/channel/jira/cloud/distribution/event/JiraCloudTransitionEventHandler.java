/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.distribution.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerTransitionEventHandler;

@Component
public class JiraCloudTransitionEventHandler implements IssueTrackerTransitionEventHandler<JiraCloudTransitionEvent> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void handle(JiraCloudTransitionEvent event) {
        logger.info("Jira server transition event");

    }
}
