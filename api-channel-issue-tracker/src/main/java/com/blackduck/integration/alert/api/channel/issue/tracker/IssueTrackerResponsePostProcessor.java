/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker;

import java.io.Serializable;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerResponse;

public interface IssueTrackerResponsePostProcessor {
    <T extends Serializable> void postProcess(IssueTrackerResponse<T> response);

}
