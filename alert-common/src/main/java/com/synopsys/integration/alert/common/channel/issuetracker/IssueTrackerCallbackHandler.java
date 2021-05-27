/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel.issuetracker;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.event.AlertDefaultEventListener;
import com.synopsys.integration.alert.api.event.AlertMessageListener;

public abstract class IssueTrackerCallbackHandler extends AlertMessageListener<IssueTrackerCallbackEvent> implements AlertDefaultEventListener {
    public static final String ISSUE_TRACKER_CALLBACK_DESTINATION_NAME = IssueTrackerCallbackHandler.class.getSimpleName();

    public IssueTrackerCallbackHandler(Gson gson) {
        super(gson, ISSUE_TRACKER_CALLBACK_DESTINATION_NAME, IssueTrackerCallbackEvent.class);
    }

}
