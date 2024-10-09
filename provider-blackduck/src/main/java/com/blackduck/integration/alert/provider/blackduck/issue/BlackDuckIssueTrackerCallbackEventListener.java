/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.issue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.common.channel.issuetracker.IssueTrackerCallbackEventListener;
import com.google.gson.Gson;

@Component
public class BlackDuckIssueTrackerCallbackEventListener extends IssueTrackerCallbackEventListener {
    @Autowired
    public BlackDuckIssueTrackerCallbackEventListener(Gson gson, TaskExecutor taskExecutor, BlackDuckIssueTrackerCallbackEventHandler callbackEventHandler) {
        super(gson, taskExecutor, callbackEventHandler);
    }

}
