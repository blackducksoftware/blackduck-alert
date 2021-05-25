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
import com.synopsys.integration.alert.api.event.MessageReceiver;

public abstract class IssueTrackerCallbackHandler extends MessageReceiver<IssueTrackerCallbackEvent> implements AlertDefaultEventListener {
    public static final String ISSUE_TRACKER_CALLBACK_DESTINATION_NAME = IssueTrackerCallbackHandler.class.getSimpleName();

    public IssueTrackerCallbackHandler(Gson gson) {
        super(gson, IssueTrackerCallbackEvent.class);
    }

    @Override
    public final String getDestinationName() {
        return ISSUE_TRACKER_CALLBACK_DESTINATION_NAME;
    }

}
