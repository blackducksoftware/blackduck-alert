/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.event;

import com.synopsys.integration.alert.api.event.AlertEvent;
import com.synopsys.integration.alert.api.event.AlertEventHandler;

public interface IssueTrackerTransitionEventHandler<T extends AlertEvent> extends AlertEventHandler<T> {
}
