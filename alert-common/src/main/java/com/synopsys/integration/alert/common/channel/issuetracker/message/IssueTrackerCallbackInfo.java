/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel.issuetracker.message;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

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
