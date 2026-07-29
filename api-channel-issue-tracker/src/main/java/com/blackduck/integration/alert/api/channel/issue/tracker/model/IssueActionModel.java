/*
 * blackduck-alert
 *
 * Copyright (c) 2026 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.model;

import java.util.UUID;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public abstract class IssueActionModel extends AlertSerializableModel {

    private final UUID alertIssueId;

    protected IssueActionModel() {
        this.alertIssueId = UUID.randomUUID();
    }

    public UUID getAlertIssueId() {
        return alertIssueId;
    }
}
