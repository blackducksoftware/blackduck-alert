/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.channel.issuetracker.message;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class IssueTrackerCallbackInfo extends AlertSerializableModel {
    private final Long providerConfigId;
    private final String callbackUrl;
    private final String blackDuckProjectVersionUrl;

    public IssueTrackerCallbackInfo(Long providerConfigId, String callbackUrl, String blackDuckProjectVersionUrl) {
        this.providerConfigId = providerConfigId;
        this.callbackUrl = callbackUrl;
        this.blackDuckProjectVersionUrl = blackDuckProjectVersionUrl;
    }

    public Long getProviderConfigId() {
        return providerConfigId;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public String getBlackDuckProjectVersionUrl() {
        return blackDuckProjectVersionUrl;
    }

}
