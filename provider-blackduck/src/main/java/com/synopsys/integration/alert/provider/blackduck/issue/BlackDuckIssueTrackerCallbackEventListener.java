/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.issue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.channel.issuetracker.IssueTrackerCallbackEventListener;

@Component
public class BlackDuckIssueTrackerCallbackEventListener extends IssueTrackerCallbackEventListener {
    @Autowired
    public BlackDuckIssueTrackerCallbackEventListener(Gson gson, BlackDuckIssueTrackerCallbackEventHandler callbackEventHandler) {
        super(gson, callbackEventHandler);
    }

}
