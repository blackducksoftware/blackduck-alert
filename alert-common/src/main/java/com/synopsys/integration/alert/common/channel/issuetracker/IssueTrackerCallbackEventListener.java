/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel.issuetracker;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.api.event.AlertMessageListener;

public abstract class IssueTrackerCallbackEventListener extends AlertMessageListener<IssueTrackerCallbackEvent> {
    public static final String ISSUE_TRACKER_CALLBACK_DESTINATION_NAME = IssueTrackerCallbackEventListener.class.getSimpleName();

    public IssueTrackerCallbackEventListener(Gson gson, AlertEventHandler<IssueTrackerCallbackEvent> eventHandler) {
        super(gson, ISSUE_TRACKER_CALLBACK_DESTINATION_NAME, IssueTrackerCallbackEvent.class, eventHandler);
    }

}
